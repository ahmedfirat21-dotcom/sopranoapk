import { FastifyInstance } from 'fastify';
import { prisma } from '../server.js';
import { z } from 'zod';

// ── Ağırlık Hesaplama (Decay Formülü) ────────────────────────────
// Her etkileşimde ağırlık artar, zaman geçtikçe decay ile azalır
function calculateWeight(interactionCount: number, lastInteractionAt: Date): number {
  const gunFarki = (Date.now() - lastInteractionAt.getTime()) / (1000 * 60 * 60 * 24);
  const decay = Math.exp(-gunFarki / 30);               // 30 gün yarı ömür
  const frekans = Math.log2(interactionCount + 1) / 10;
  return Math.min(10.0, (frekans + decay) * 5);          // 0.0 → 10.0 arası
}

// ── Bağlantı Route'ları ──────────────────────────────────────────
export async function connectionRoutes(server: FastifyInstance) {

  // GET /api/connections/:userId — Ağaç bağlantıları (ağırlığa göre sıralı)
  server.get('/:userId', async (request) => {
    const { userId } = z.object({ userId: z.string().uuid() }).parse(request.params);

    // Her iki yöndeki bağlantıları al
    const connections = await prisma.connection.findMany({
      where: {
        OR: [
          { userA_id: userId },
          { userB_id: userId },
        ],
      },
      include: {
        userA: {
          select: { id: true, username: true, displayName: true, avatarUrl: true, auraColor: true, resonanceScore: true, isOnline: true },
        },
        userB: {
          select: { id: true, username: true, displayName: true, avatarUrl: true, auraColor: true, resonanceScore: true, isOnline: true },
        },
      },
      orderBy: { connectionWeight: 'desc' },
    });

    // Karşı tarafı döndür
    return connections.map((c) => ({
      id: c.id,
      user: c.userA_id === userId ? c.userB : c.userA,
      connectionWeight: c.connectionWeight,
      interactionCount: c.interactionCount,
      lastInteractionAt: c.lastInteractionAt,
    }));
  });

  // POST /api/connections — Yeni bağlantı oluştur (çift yönlü tek kayıt)
  server.post('/', {
    preHandler: [(server as any).authenticate],
  }, async (request, reply) => {
    const userId = (request as any).user.id;
    const { targetUserId } = z.object({ targetUserId: z.string().uuid() }).parse(request.body);

    if (userId === targetUserId) {
      return reply.status(400).send({ error: 'Kendinizle bağlantı kuramazsınız' });
    }

    // Sıralı ID'ler ile tekil kayıt (A < B sıralaması)
    const [a, b] = [userId, targetUserId].sort();

    const connection = await prisma.connection.upsert({
      where: { userA_id_userB_id: { userA_id: a, userB_id: b } },
      create: { userA_id: a, userB_id: b },
      update: {},
    });

    return { connectionId: connection.id };
  });

  // PATCH /api/connections/:id/interact — Etkileşim kaydı → ağırlık güncelle
  server.patch('/:id/interact', {
    preHandler: [(server as any).authenticate],
  }, async (request) => {
    const { id } = z.object({ id: z.string().uuid() }).parse(request.params);

    const conn = await prisma.connection.update({
      where: { id },
      data: {
        interactionCount: { increment: 1 },
        lastInteractionAt: new Date(),
      },
    });

    const yeniAgirlik = calculateWeight(conn.interactionCount, conn.lastInteractionAt);

    const guncellenmis = await prisma.connection.update({
      where: { id },
      data: { connectionWeight: yeniAgirlik },
    });

    return { connectionWeight: guncellenmis.connectionWeight, interactionCount: guncellenmis.interactionCount };
  });

  // DELETE /api/connections/:id — Bağlantı sil
  server.delete('/:id', {
    preHandler: [(server as any).authenticate],
  }, async (request, reply) => {
    const { id } = z.object({ id: z.string().uuid() }).parse(request.params);
    await prisma.connection.delete({ where: { id } });
    return reply.status(204).send();
  });
}
