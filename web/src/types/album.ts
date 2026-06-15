export interface Album {
  id: number;
  activityId: number;
  url: string;
  thumbnailUrl: string;
  description?: string;
  sortOrder: number;
}

export interface AlbumUploadResponse {
  id: number;
  activityId: number;
  url: string;
}
