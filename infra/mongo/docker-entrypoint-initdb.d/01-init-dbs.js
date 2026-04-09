// --- USER SERVICE ---
db = db.getSiblingDB("userdb");
db.createCollection("users");

// --- PRODUCT SERVICE ---
db = db.getSiblingDB("productdb");
db.createCollection("products");

// --- ORDER SERVICE ---
db = db.getSiblingDB("orderdb");
db.createCollection("orders");

// --- NOTIF SERVICE ---
db = db.getSiblingDB("notifdb");
db.createCollection("user_projections");