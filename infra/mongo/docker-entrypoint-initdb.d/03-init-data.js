// --- USER SERVICE ---
db = db.getSiblingDB("userdb");
db.users.insertMany([
  { username: "admin", email: "admin@example.com", role: "ADMIN" },
  { username: "user1", email: "user1@example.com", role: "USER" }
]);

// --- PRODUCT SERVICE ---
db = db.getSiblingDB("productdb");
db.products.insertMany([
  { name: "Laptop", category: "Electronics", price: 1200 },
  { name: "Phone", category: "Electronics", price: 800 }
]);

// --- ORDER SERVICE ---
db = db.getSiblingDB("orderdb");
db.orders.insertMany([
  { userId: "1", productId: "1", quantity: 1, status: "CREATED", createdAt: new Date() },
  { userId: "2", productId: "2", quantity: 2, status: "CREATED", createdAt: new Date() }
]);