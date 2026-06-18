import { useState, useEffect } from 'react';
import { X, Upload, Trash2, Image as ImageIcon } from 'lucide-react';
import { getActivityAlbums, uploadAlbum, deleteAlbum } from '@/api/album';
import { useToastStore } from '@/components/Toast';
import apiClient from '@/api/client';
import type { Album } from '@/types/album';

/** 从 apiClient 的 baseURL 提取服务器根地址，用于拼接图片路径 */
const SERVER_ORIGIN = apiClient.defaults.baseURL?.replace(/\/api\/v1\/?$/, '') || '';

/** 根据相册 URL 生成完整图片地址 */
const getImageUrl = (url: string) => `${SERVER_ORIGIN}/uploads/${url}`;

interface ActivityAlbumProps {
  activityId: number;
  isOwner: boolean;
}

export default function ActivityAlbum({ activityId, isOwner }: ActivityAlbumProps) {
  const { addToast } = useToastStore();
  const [albums, setAlbums] = useState<Album[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [deleteConfirmId, setDeleteConfirmId] = useState<number | null>(null);
  const [selectedImage, setSelectedImage] = useState<string | null>(null);
  const [description, setDescription] = useState('');

  useEffect(() => {
    loadAlbums();
  }, [activityId]);

  const loadAlbums = async () => {
    try {
      const res = await getActivityAlbums(activityId);
      setAlbums(res.data.data || []);
    } catch (err) {
      console.error('加载相册失败', err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploading(true);
    try {
      await uploadAlbum(activityId, file, description || undefined);
      setDescription('');
      await loadAlbums();
    } catch (err) {
      console.error('上传失败', err);
      addToast('error', '上传失败，请重试');
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (albumId: number) => {
    try {
      await deleteAlbum(albumId);
      addToast('success', '图片已删除');
      await loadAlbums();
    } catch (err) {
      console.error('删除失败', err);
      addToast('error', '删除失败，请重试');
    } finally {
      setDeleteConfirmId(null);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  return (
    <div className="mt-8">
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-lg font-semibold flex items-center gap-2">
          <ImageIcon className="w-5 h-5" />
          活动相册 ({albums.length})
        </h3>
        {isOwner && (
          <label className="cursor-pointer bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 flex items-center gap-2">
            <Upload className="w-4 h-4" />
            {uploading ? '上传中...' : '上传图片'}
            <input
              type="file"
              accept="image/*"
              onChange={handleUpload}
              disabled={uploading}
              className="hidden"
            />
          </label>
        )}
      </div>

      {albums.length === 0 ? (
        <div className="text-center py-8 text-gray-500 bg-gray-50 rounded-lg">
          <ImageIcon className="w-12 h-12 mx-auto mb-2 opacity-50" />
          <p>暂无相册图片</p>
          {isOwner && <p className="text-sm">点击上方按钮上传活动照片</p>}
        </div>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          {albums.map((album) => (
            <div key={album.id} className="relative group">
              <img
                src={getImageUrl(album.url)}
                alt={album.description || '活动照片'}
                className="w-full h-40 object-cover rounded-lg cursor-pointer hover:opacity-90 transition-opacity"
                onClick={() => setSelectedImage(getImageUrl(album.url))}
              />
              {isOwner && (
                <button
                  onClick={() => setDeleteConfirmId(album.id)}
                  className="absolute top-2 right-2 p-1.5 bg-red-500 text-white rounded-lg opacity-0 group-hover:opacity-100 transition-opacity hover:bg-red-600"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              )}
              {album.description && (
                <p className="text-sm text-gray-600 mt-1 truncate">{album.description}</p>
              )}
            </div>
          ))}
        </div>
      )}

      {/* 删除确认弹窗 */}
      {deleteConfirmId !== null && (
        <div
          className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
          onClick={() => setDeleteConfirmId(null)}
        >
          <div
            className="bg-white rounded-2xl p-6 shadow-xl max-w-sm mx-4"
            onClick={(e) => e.stopPropagation()}
          >
            <h3 className="text-lg font-semibold text-gray-900 mb-2">确认删除</h3>
            <p className="text-gray-600 mb-6">确定要删除这张图片吗？此操作不可撤销。</p>
            <div className="flex gap-3 justify-end">
              <button
                onClick={() => setDeleteConfirmId(null)}
                className="px-4 py-2 rounded-lg border border-gray-300 text-gray-700 hover:bg-gray-50 transition-colors"
              >
                取消
              </button>
              <button
                onClick={() => handleDelete(deleteConfirmId)}
                className="px-4 py-2 rounded-lg bg-red-500 text-white hover:bg-red-600 transition-colors"
              >
                删除
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 图片预览弹窗 */}
      {selectedImage && (
        <div
          className="fixed inset-0 bg-black bg-opacity-80 flex items-center justify-center z-50"
          onClick={() => setSelectedImage(null)}
        >
          <button
            className="absolute top-4 right-4 p-2 text-white hover:bg-white hover:bg-opacity-20 rounded-full"
            onClick={() => setSelectedImage(null)}
          >
            <X className="w-6 h-6" />
          </button>
          <img
            src={selectedImage}
            alt="预览"
            className="max-w-[90vw] max-h-[90vh] object-contain"
            onClick={(e) => e.stopPropagation()}
          />
        </div>
      )}
    </div>
  );
}
