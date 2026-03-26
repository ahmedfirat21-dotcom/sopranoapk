import { FastifyInstance } from 'fastify';
import { z } from 'zod';
import { S3Client, PutObjectCommand } from '@aws-sdk/client-s3';
import { getSignedUrl } from '@aws-sdk/s3-request-presigner';

function getR2Client(): S3Client | null {
  const accountId = process.env.R2_ACCOUNT_ID;
  const accessKeyId = process.env.R2_ACCESS_KEY_ID;
  const secretAccessKey = process.env.R2_SECRET_ACCESS_KEY;

  if (!accountId || !accessKeyId || !secretAccessKey) return null;

  return new S3Client({
    region: 'auto',
    endpoint: `https://${accountId}.r2.cloudflarestorage.com`,
    credentials: { accessKeyId, secretAccessKey },
  });
}

// ── Medya Route'ları ─────────────────────────────────────────────
export async function mediaRoutes(server: FastifyInstance) {

  // POST /api/media/upload-url
  server.post('/upload-url', {
    preHandler: [(server as any).authenticate],
  }, async (request) => {
    const userId = (request as any).user.id;
    const body = z.object({
      mediaType: z.enum(['AUDIO', 'VIDEO']),
      extension: z.enum(['webm', 'mp4', 'ogg', 'm4a', 'wav']).default('webm'),
      duration: z.number().min(1).max(60), // durationSec yerine duration oldu
    }).parse(request.body);

    const r2 = getR2Client();
    const bucketName = process.env.R2_BUCKET_NAME || 'soprano-media';
    const publicUrl = process.env.R2_PUBLIC_URL || `https://${bucketName}.r2.dev`;

    const mediaId = `${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
    const key = `media/${userId}/${mediaId}.${body.extension}`;

    if (r2) {
      const command = new PutObjectCommand({
        Bucket: bucketName,
        Key: key,
        ContentType: body.mediaType === 'AUDIO' ? `audio/${body.extension}` : `video/${body.extension}`,
        Metadata: {
          userId,
          duration: String(body.duration),
        },
      });

      const uploadUrl = await getSignedUrl(r2, command, { expiresIn: 300 });

      return {
        mediaId,
        uploadUrl,
        cdnUrl: `${publicUrl}/${key}`,
        expiresIn: 300,
      };
    } else {
      return {
        mediaId,
        uploadUrl: `http://localhost:3000/mock-upload/${key}`,
        cdnUrl: `http://localhost:3000/mock-cdn/${key}`,
        expiresIn: 300,
        _dev: true,
      };
    }
  });
}
