import { FastifyInstance } from 'fastify';
import { prisma } from '../server.js';
import { z } from 'zod';

// ── User Routes ──────────────────────────────────────────────────
export async function userRoutes(server: FastifyInstance) {

  // GET /api/users/:id
  server.get('/:id', async (request, reply) => {
    const { id } = z.object({ id: z.string().uuid() }).parse(request.params);

    const user = await prisma.user.findUnique({
      where: { id },
      select: {
        id: true,
        username: true,
        displayName: true,
        avatarUrl: true,
        bio: true,
        resonanceScore: true,
        auraColor: true,
        isOnline: true,
        lastSeenAt: true,
        createdAt: true,
        showcaseEchoes: {
          include: { echo: { select: { id: true, mediaUrl: true, mediaType: true, duration: true } } },
          orderBy: { order: 'asc' },
          take: 5,
        },
      },
    });

    if (!user) return reply.status(404).send({ error: 'Kullanıcı bulunamadı' });
    return user;
  });

  // PATCH /api/users/me
  server.patch('/me', {
    preHandler: [(server as any).authenticate],
  }, async (request) => {
    const userId = (request as any).user.id;
    const body = z.object({
      displayName: z.string().min(1).max(50).optional(),
      bio: z.string().max(300).optional(),
      avatarUrl: z.string().url().optional(),
      auraColor: z.string().optional(),
    }).parse(request.body);

    const user = await prisma.user.update({
      where: { id: userId },
      data: body,
      select: {
        id: true,
        username: true,
        displayName: true,
        avatarUrl: true,
        bio: true,
        resonanceScore: true,
        auraColor: true,
      },
    });

    return user;
  });

  // GET /api/users/search?q=
  server.get('/search', async (request) => {
    const { q } = z.object({ q: z.string().min(1) }).parse(request.query);

    const users = await prisma.user.findMany({
      where: {
        OR: [
          { username: { contains: q, mode: 'insensitive' } },
          { displayName: { contains: q, mode: 'insensitive' } },
        ],
      },
      select: {
        id: true,
        username: true,
        displayName: true,
        avatarUrl: true,
        auraColor: true,
        resonanceScore: true,
        isOnline: true,
      },
      take: 20,
      orderBy: { resonanceScore: 'desc' },
    });

    return users;
  });
}
