import { FastifyInstance } from 'fastify';
import { prisma } from '../server.js';
import { z } from 'zod';

// ── Fısıltı Route'ları (Premium DM) ─────────────────────────────
export async function whisperRoutes(server: FastifyInstance) {

  // GET /api/whispers/:contactId — Fısıltı geçmişi
  server.get('/:contactId', {
    preHandler: [(server as any).authenticate],
  }, async (request) => {
    const userId = (request as any).user.id;
    const { contactId } = z.object({ contactId: z.string().uuid() }).parse(request.params);

    const whispers = await prisma.whisper.findMany({
      where: {
        OR: [
          { senderId: userId, receiverId: contactId },
          { senderId: contactId, receiverId: userId },
        ],
      },
      orderBy: { createdAt: 'asc' },
      take: 100,
    });

    // Okunmamışları "rezone olmuş" olarak işaretle
    await prisma.whisper.updateMany({
      where: {
        senderId: contactId,
        receiverId: userId,
        isResonated: false,
      },
      data: {
        isResonated: true,
        resonatedAt: new Date(),
      },
    });

    return whispers;
  });

  // POST /api/whispers — Fısıltı gönder
  server.post('/', {
    preHandler: [(server as any).authenticate],
  }, async (request, reply) => {
    const userId = (request as any).user.id;
    const body = z.object({
      receiverId: z.string().uuid(),
      mediaUrl: z.string().url(),
      mediaType: z.enum(['AUDIO', 'VIDEO']).default('AUDIO'),
      duration: z.number().min(1).max(60),
    }).parse(request.body);

    if (userId === body.receiverId) {
      return reply.status(400).send({ error: 'Kendinize fısıltı gönderemezsiniz' });
    }

    const whisper = await prisma.whisper.create({
      data: {
        senderId: userId,
        ...body,
      },
    });

    // Bağlantı ağırlığını güncelle (varsa)
    const [a, b] = [userId, body.receiverId].sort();
    await prisma.connection.updateMany({
      where: { userA_id: a, userB_id: b },
      data: {
        interactionCount: { increment: 1 },
        lastInteractionAt: new Date(),
      },
    });

    return whisper;
  });

  // GET /api/whispers/contacts/list — DM kişi listesi
  server.get('/contacts/list', {
    preHandler: [(server as any).authenticate],
  }, async (request) => {
    const userId = (request as any).user.id;

    const contacts = await prisma.$queryRaw`
      SELECT DISTINCT ON (contact_id) 
        contact_id,
        w.id as last_whisper_id,
        w."mediaType" as media_type,
        w."duration" as duration,
        w."isResonated" as is_resonated,
        w."createdAt" as last_message_at,
        u.username,
        u."displayName" as display_name,
        u."avatarUrl" as avatar_url,
        u."auraColor" as aura_color,
        u."isOnline" as is_online
      FROM (
        SELECT "senderId" as contact_id, id, "mediaType", "duration", "isResonated", "createdAt"
        FROM "Whisper" WHERE "receiverId" = ${userId}
        UNION ALL
        SELECT "receiverId" as contact_id, id, "mediaType", "duration", "isResonated", "createdAt"
        FROM "Whisper" WHERE "senderId" = ${userId}
      ) w
      JOIN "User" u ON u.id = w.contact_id
      ORDER BY contact_id, w."createdAt" DESC
    `;

    return contacts;
  });
}
