import Fastify from 'fastify';
import cors from '@fastify/cors';
import jwt from '@fastify/jwt';
import websocket from '@fastify/websocket';
import { PrismaClient } from '@prisma/client';
import { authRoutes } from './routes/auth.js';
import { userRoutes } from './routes/users.js';
import { connectionRoutes } from './routes/connections.js';
import { echoRoutes } from './routes/echoes.js';
import { whisperRoutes } from './routes/whispers.js';
import { roomRoutes } from './routes/rooms.js';
import { mediaRoutes } from './routes/media.js';
import { wsGateway } from './ws/gateway.js';

// ── Prisma Client ────────────────────────────────────────────────
export const prisma = new PrismaClient({
  log: ['warn', 'error'],
});

// ── Fastify Server ───────────────────────────────────────────────
const server = Fastify({
  logger: {
    transport: {
      target: 'pino-pretty',
    },
  },
});

async function bootstrap() {
  // ── Plugins ──
  await server.register(cors, { origin: true });

  await server.register(jwt, {
    secret: process.env.JWT_SECRET || 'dev-secret-min-32-characters-long!!',
  });

  await server.register(websocket);

  // ── Auth decorator ──
  server.decorate('authenticate', async (request: any, reply: any) => {
    try {
      await request.jwtVerify();
    } catch (err) {
      reply.status(401).send({ error: 'Yetkisiz erişim' });
    }
  });

  // ── REST Routes ──
  await server.register(authRoutes,       { prefix: '/api/auth' });
  await server.register(userRoutes,       { prefix: '/api/users' });
  await server.register(connectionRoutes, { prefix: '/api/connections' });
  await server.register(echoRoutes,       { prefix: '/api/echoes' });
  await server.register(whisperRoutes,    { prefix: '/api/whispers' });
  await server.register(roomRoutes,       { prefix: '/api/rooms' });
  await server.register(mediaRoutes,      { prefix: '/api/media' });

  // ── WebSocket Gateway ──
  await server.register(wsGateway);

  // ── Health Check ──
  server.get('/health', async () => ({ status: 'ok', timestamp: new Date().toISOString() }));

  // ── Start ──
  const port = parseInt(process.env.PORT || '3000');
  const host = process.env.HOST || '0.0.0.0';

  await server.listen({ port, host });
  server.log.info(`🎙️ SopranoChat Backend çalışıyor: http://${host}:${port}`);
}

bootstrap().catch((err) => {
  console.error('Server başlatılamadı:', err);
  process.exit(1);
});

// Graceful shutdown
const signals = ['SIGINT', 'SIGTERM'];
signals.forEach((signal) => {
  process.on(signal, async () => {
    await prisma.$disconnect();
    await server.close();
    process.exit(0);
  });
});
