import { FastifyInstance } from 'fastify';
import { prisma } from '../server.js';
import { z } from 'zod';

// ── Canlı Odalar (FrequencyRoom) Route'ları ──────────────────────
export async function roomRoutes(server: FastifyInstance) {

  // POST /api/rooms — Oda oluştur
  server.post('/', {
    preHandler: [(server as any).authenticate],
  }, async (request) => {
    const userId = (request as any).user.id;
    const body = z.object({
      title: z.string().min(2).max(100),
      maxSeats: z.number().min(2).max(50).default(12),
    }).parse(request.body);

    // Unique LiveKit room adı oluştur
    const liveKitRoomName = `soprano_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;

    const room = await prisma.frequencyRoom.create({
      data: {
        hostId: userId,
        title: body.title,
        maxSeats: body.maxSeats,
        isActive: true,
        liveKitRoomName,
        participants: {
          create: {
            userId,
            role: 'HOST',
          },
        },
      },
      include: {
        participants: {
          include: {
            user: {
              select: { id: true, username: true, displayName: true, avatarUrl: true, auraColor: true },
            },
          },
        },
      },
    });

    return room;
  });

  // POST /api/rooms/:id/join — Odaya katıl + LiveKit token al
  server.post('/:id/join', {
    preHandler: [(server as any).authenticate],
  }, async (request, reply) => {
    const userId = (request as any).user.id;
    const username = (request as any).user.username;
    const { id } = z.object({ id: z.string().uuid() }).parse(request.params);

    const room = await prisma.frequencyRoom.findUnique({
      where: { id },
      include: { _count: { select: { participants: true } } },
    });

    if (!room || !room.isActive) {
      return reply.status(404).send({ error: 'Oda bulunamadı veya kapalı' });
    }

    if (room._count.participants >= room.maxSeats) {
      return reply.status(409).send({ error: 'Oda dolu' });
    }

    // Katılımcı ekle
    await prisma.roomParticipant.upsert({
      where: { roomId_userId: { roomId: id, userId } },
      create: { roomId: id, userId, role: 'LISTENER' },
      update: { joinedAt: new Date() },
    });

    // LiveKit token üret
    let livekitToken: string | null = null;

    try {
      const { AccessToken } = await import('livekit-server-sdk');
      const apiKey = process.env.LIVEKIT_API_KEY;
      const apiSecret = process.env.LIVEKIT_API_SECRET;

      if (apiKey && apiSecret && room.liveKitRoomName) {
        const token = new AccessToken(apiKey, apiSecret, {
          identity: userId,
          name: username,
        });
        token.addGrant({
          roomJoin: true,
          room: room.liveKitRoomName,
          canPublish: false, // Varsayılan olarak LISTENER konuşamaz
          canSubscribe: true,
        });
        livekitToken = await token.toJwt();
      }
    } catch {
      // LiveKit yapılandırılmamışsa atla
    }

    return {
      room: {
        id: room.id,
        title: room.title,
        liveKitRoomName: room.liveKitRoomName,
      },
      livekitToken,
      livekitUrl: process.env.LIVEKIT_URL || null,
    };
  });

  // POST /api/rooms/:id/leave
  server.post('/:id/leave', {
    preHandler: [(server as any).authenticate],
  }, async (request) => {
    const userId = (request as any).user.id;
    const { id } = z.object({ id: z.string().uuid() }).parse(request.params);

    await prisma.roomParticipant.deleteMany({
      where: { roomId: id, userId },
    });

    // Odada kimse kalmadıysa odayı kapat
    const remaining = await prisma.roomParticipant.count({
      where: { roomId: id },
    });

    if (remaining === 0) {
      await prisma.frequencyRoom.update({
        where: { id },
        data: { isActive: false },
      });
    }

    return { left: true };
  });

  // GET /api/rooms/:id — Oda detayı
  server.get('/:id', async (request, reply) => {
    const { id } = z.object({ id: z.string().uuid() }).parse(request.params);

    const room = await prisma.frequencyRoom.findUnique({
      where: { id },
      include: {
        host: { select: { id: true, username: true, displayName: true, avatarUrl: true, auraColor: true } },
        participants: {
          include: {
            user: {
              select: { id: true, username: true, displayName: true, avatarUrl: true, auraColor: true },
            },
          },
          orderBy: { joinedAt: 'asc' },
        },
      },
    });

    if (!room) return reply.status(404).send({ error: 'Oda bulunamadı' });
    return room;
  });

  // GET /api/rooms?active=true — Aktif odaları listele
  server.get('/', async (request) => {
    const { active } = z.object({ active: z.coerce.boolean().optional() }).parse(request.query);

    const rooms = await prisma.frequencyRoom.findMany({
      where: active ? { isActive: true } : {},
      include: {
        host: { select: { id: true, username: true, displayName: true, avatarUrl: true, auraColor: true } },
        _count: { select: { participants: true } },
      },
      orderBy: { createdAt: 'desc' },
      take: 50,
    });

    return rooms;
  });
}
