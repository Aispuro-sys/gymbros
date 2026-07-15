const { PrismaClient } = require('@prisma/client');
const bcrypt = require('bcryptjs');

const prisma = new PrismaClient();

async function main() {
  const hashed = await bcrypt.hash('admin123', 10);

  const admin = await prisma.user.upsert({
    where: { email: 'admin@gymbros.com' },
    update: {},
    create: {
      username: 'admin',
      email: 'admin@gymbros.com',
      password: hashed,
      age: 24,
      height_cm: 178.0,
      weight_kg: 85.0,
      goal: 'BULKING',
    },
  });

  console.log('Seed complete. Demo user:', admin.email);
}

main()
  .catch((e) => console.error(e))
  .finally(async () => await prisma.$disconnect());
