import { FastifyInstance } from 'fastify';
import { prisma } from '../server.js';
import bcrypt from 'bcryptjs';
import { z } from 'zod';

// ── Validation Schemas ───────────────────────────────────────────
const registerSchema = z.object({
  username: z.string().min(3).max(24).regex(/^[a-zA-Z0-9_]+$/),
  email: z.string().email(),
  password: z.string().min(6).max(128),
  displayName: z.string().min(1).max(50).optional(),
});

const loginSchema = z.object({
  email: z.string().email(),
  password: z.string(),
});

// ── Auth Routes ──────────────────────────────────────────────────
export async function authRoutes(server: FastifyInstance) {

  // POST /api/auth/register
  server.post('/register', async (request, reply) => {
    const body = registerSchema.parse(request.body);

    // Check uniqueness
    const existing = await prisma.user.findFirst({
      where: { OR: [{ username: body.username }, { email: body.email }] },
    });
    if (existing) {
      return reply.status(409).send({
        error: existing.username === body.username
          ? 'Bu kullanıcı adı zaten kullanılıyor'
          : 'Bu e-posta zaten kayıtlı',
      });
    }

    const passwordHash = await bcrypt.hash(body.password, 12);

    const user = await prisma.user.create({
      data: {
        username: body.username,
        email: body.email,
        passwordHash,
        displayName: body.displayName || body.username,
      },
      select: { id: true, username: true, email: true, displayName: true },
    });

    const token = server.jwt.sign({ id: user.id, username: user.username });

    return reply.status(201).send({ user, token });
  });

  // POST /api/auth/login
  server.post('/login', async (request, reply) => {
    const body = loginSchema.parse(request.body);

    const user = await prisma.user.findUnique({
      where: { email: body.email },
    });
    if (!user) {
      return reply.status(401).send({ error: 'Geçersiz e-posta veya şifre' });
    }

    const valid = await bcrypt.compare(body.password, user.passwordHash);
    if (!valid) {
      return reply.status(401).send({ error: 'Geçersiz e-posta veya şifre' });
    }

    // Update online status
    await prisma.user.update({
      where: { id: user.id },
      data: { isOnline: true, lastSeenAt: new Date() },
    });

    const token = server.jwt.sign({ id: user.id, username: user.username });

    return {
      user: {
        id: user.id,
        username: user.username,
        email: user.email,
        displayName: user.displayName,
        avatarUrl: user.avatarUrl,
        resonanceScore: user.resonanceScore,
      },
      token,
    };
  });

  // POST /api/auth/check-username
  server.post('/check-username', async (request) => {
    const { username } = z.object({ username: z.string().min(3) }).parse(request.body);
    const exists = await prisma.user.findUnique({ where: { username } });
    return { available: !exists };
  });
}
