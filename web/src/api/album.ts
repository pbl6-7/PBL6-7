import apiClient from './client';
import type { Album } from '@/types/album';

const BASE_URL = '/albums';

export const getActivityAlbums = async (activityId: number): Promise<{ data: { data: Album[] } }> => {
  return apiClient.get(`${BASE_URL}/activities/${activityId}`);
};

export const uploadAlbum = async (
  activityId: number,
  file: File,
  description?: string
): Promise<{ data: { data: Album } }> => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('activityId', String(activityId));
  if (description) {
    formData.append('description', description);
  }
  return apiClient.post(`${BASE_URL}/upload`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const deleteAlbum = async (albumId: number): Promise<void> => {
  return apiClient.delete(`${BASE_URL}/${albumId}`);
};
