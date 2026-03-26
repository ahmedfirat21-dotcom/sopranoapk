import { FastifyInstance } from 'fastify';
import { prisma } from '../server.js';
import { z } from 'zod';

// ── Yankı Route'ları ─────────────────────────────────────────────
export async function echoRoutes(server: FastifyInstance) {

  // GET /api/echoes?cursor=&limit=
  server.get('/', async (request) => {
    const query = z.object({
      cursor: z.string().uuid().optional(),
      limit: z.coerce.number().min(1).max(50).default(20),
    }).parse(request.query);

    const echoes = await prisma.echo.findMany({
      where: { parentId: null }, // Sadece kök yankılar
      include: {
        author: {
          select: { id: true, username: true, displayName: true, avatarUrl: true, auraColor: true },
        },
        _count: { select: { replies: true, resonances: true } },
      },
      orderBy: { createdAt: 'desc' },
      take: query.limit,
      ...(query.cursor ? { cursor: { id: query.cursor }, skip: 1 } : {}),
    });

    return {
      echoes,
      nextCursor: echoes.length === query.limit ? echoes[echoes.length - 1].id : null,
    };
  });

  // GET /api/echoes/:id/chain — Yankı zinciri (ağaç yapısı, 3 seviye derinlik)
  server.get('/:id/chain', async (request, reply) => {
    const { id } = z.object({ id: z.string().uuid() }).parse(request.params);

    const root = await prisma.echo.findUnique({
      where: { id },
      include: {
        author: {
          select: { id: true, username: true, displayName: true, avatarUrl: true, auraColor: true },
        },
        replies: {
          include: {
            author: {
              select: { id: true, username: true, displayName: true, avatarUrl: true, auraColor: true },
            },
            replies: {
              include: {
                author: {
                  select: { id: true, username: true, displayName: true, avatarUrl: true, auraColor: true },
                },
                _count: { select: { replies: true } },
              },
            },
            _count: { select: { replies: true, resonances: true } },
          },
        },
        _count: { select: { replies: true, resonances: true } },
      },
    });

    if (!root) return reply.status(404).send({ error: 'Yankı bulunamadı' });
    return root;
  });

  // POST /api/echoes — Yeni yankı oluştur
  server.post('/', {
    preHandler: [(server as any).authenticate],
  }, async (request) => {
    const userId = (request as any).user.id;
    const body = z.object({
      mediaUrl: z.string().url(),
      mediaType: z.enum(['AUDIO', 'VIDEO']).default('AUDIO'),
      duration: z.number().min(1).max(30),
      parentId: z.string().uuid().optional(),
    }).parse(request.body);

    const echo = await prisma.echo.create({
      data: {
        authorId: userId,
        ...body,
      },
      include: {
        author: {
          select: { id: true, username: true, displayName: true, avatarUrl: true, auraColor: true },
        },
      },
    });

    // Yazarın resonanceScore'unu artır
    await prisma.user.update({
      where: { id: userId },
      data: { resonanceScore: { increment: 1 } },
    });

    return echo;
  });

  // POST /api/echoes/:id/resonate — Dinlendi/rezone oldu işaretle
  server.post('/:id/resonate', {
    preHandler: [(server as any).authenticate],
  }, async (request) => {
    const userId = (request as any).user.id;
    const { id } = z.object({ id: z.string().uuid() }).parse(request.params);

    await prisma.echoResonance.upsert({
      where: { echoId_userId: { echoId: id, userId } },
      create: { echoId: id, userId },
      update: { listenedAt: new Date() },
    });

    await prisma.echo.update({
      where: { id },
      data: { resonanceCount: { increment: 1 } },
    });

    return { resonated: true };
  });
}
