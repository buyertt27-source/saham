const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validWatchlistPayload(uid, now) {
  return {
    userId: uid,
    ticker: "NVDA",
    companyName: "NVIDIA Corporation",
    exchange: "NASDAQ",
    country: "United States",
    sector: "Semiconductors",
    currency: "USD",
    lastPrice: 138.5,
    changePercent: 3.42,
    chanceUp: 68,
    chanceDown: 20,
    chanceSideways: 12,
    sortOrder: 0,
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated user cannot read or write watchlist", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("watchlist").get()
  );
});

test("Authenticated owner can create and query own watchlist item", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  const docRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("watchlist")
    .doc("NVDA");

  await assertSucceeds(docRef.set(validWatchlistPayload(ALICE_UID, now)));
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("watchlist")
      .where("userId", "==", ALICE_UID)
      .get()
  );
});

test("Cross-user access to another user's watchlist is denied", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("users")
      .doc(ALICE_UID)
      .collection("watchlist")
      .doc("NVDA")
      .set(validWatchlistPayload(ALICE_UID, now));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("watchlist")
      .doc("NVDA")
      .get()
  );
});

test("Watchlist item fails if probabilities do not sum to 100", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  const badPayload = {
    ...validWatchlistPayload(ALICE_UID, now),
    chanceUp: 80,
    chanceDown: 20,
    chanceSideways: 10, // sum = 110
  };
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("watchlist")
      .doc("NVDA")
      .set(badPayload)
  );
});

test("Shadow update with unauthorized extra field is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("watchlist")
      .doc("NVDA")
      .set({
        ...validWatchlistPayload(ALICE_UID, now),
        isAdminHack: true,
      })
  );
});
