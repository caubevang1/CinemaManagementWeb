import { http } from '../utils/baseUrl';

export const searchTmdb = (query) => http.get(`/tmdb/search?query=${encodeURIComponent(query)}`);

export const getTmdbDetail = (tmdbId) => http.get(`/tmdb/detail/${tmdbId}`);

export const importFromTmdb = (tmdbId) => http.post(`/tmdb/import/${tmdbId}`);
