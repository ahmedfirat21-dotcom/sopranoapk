import { FastifyInstance } from 'fastify';
import { prisma } from '../server.js';

const onlineUsers = new Map<string, { userId: string; ws: any }>();

export async function wsGateway(server: FastifyInstance) {
  server.get('/ws', { websocket: true }, (socket, request) => {
    let userId: string | null = null;

    socket.on('message', async (raw: Buffer) => {
      try {
        const msg = JSON.parse(raw.toString());

        switch (msg.type) {
          case 'auth': {
            try {
              const decoded = server.jwt.verify<{ id: string; username: string }>(msg.token);
              userId = decoded.id;

              onlineUsers.set(userId, { userId, ws: socket });

              await prisma.user.update({
                where: { id: userId },
                data: { isOnline: true, lastSeenAt: new Date() },
              });

              socket.send(JSON.stringify({
                type: 'auth_ok',
                userId,
                onlineCount: onlineUsers.size,
              }));
            } catch {
              socket.send(JSON.stringify({ type: 'auth_error', error: 'Geçersiz token' }));
            }
            break;
          }

          case 'ping': {
            socket.send(JSON.stringify({ type: 'pong', timestamp: Date.now() }));
            break;
          }

          case 'speaker_levels': {
            // Canlı oda ses seviyelerini katılımcılara yay (broadcast)
            if (msg.roomId && msg.speakers) {
              const participants = await prisma.roomParticipant.findMany({
                where: { roomId: msg.roomId },
                select: { userId: true },
              });

              const payload = JSON.stringify({
                type: 'speaker_update',
                roomId: msg.roomId,
                speakers: msg.speakers,
              });

              for (const p of participants) {
                const conn = onlineUsers.get(p.userId);
                if (conn && conn.ws.readyState === 1) {
                  conn.ws.send(payload);
                }
              }
            }
            break;
          }

          case 'whisper_sent': {
            if (userId && msg.receiverId) {
              const receiverConn = onlineUsers.get(msg.receiverId);
              if (receiverConn && receiverConn.ws.readyState === 1) {
                receiverConn.ws.send(JSON.stringify({
                  type: 'whisper_received',
                  from: userId,
                  whisperId: msg.whisperId,
                  mediaType: msg.mediaType,
                }));
              }
            }
            break;
          }

          case 'call_invite': {
            if (userId && msg.targetUserId && msg.roomId) {
              const targetConn = onlineUsers.get(msg.targetUserId);
              if (targetConn && targetConn.ws.readyState === 1) {
                const caller = await prisma.user.findUnique({
                  where: { id: userId },
                  select: { displayName: true, username: true, avatarUrl: true, auraColor: true },
                });

                targetConn.ws.send(JSON.stringify({
                  type: 'incoming_call',
                  caller: { id: userId, ...caller },
                  roomId: msg.roomId,
                }));
              }
            }
            break;
          }

          default:
            socket.send(JSON.stringify({ type: 'error', error: `Bilinmeyen mesaj tipi: ${msg.type}` }));
        }
      } catch {
        socket.send(JSON.stringify({ type: 'error', error: 'Geçersiz mesaj formatı' }));
      }
    });

    socket.on('close', async () => {
      if (userId) {
        onlineUsers.delete(userId);
        try {
          await prisma.user.update({
            where: { id: userId },
            data: { isOnline: false, lastSeenAt: new Date() },
          });
        } catch {}
      }
    });
  });
}
