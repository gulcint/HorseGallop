export type HomeRecentActivityDto = {
  id: string;
  title: string;
  dateLabel: string;
  timeLabel: string;
  durationMin: number;
  distanceKm: number;
};

export type HomeStatsDto = {
  totalRides: number;
  totalDistanceKm: number;
  totalDurationMin: number;
  totalCalories: number;
  favoriteBarn: string | null;
};

export type HomeDashboardDto = {
  stats: HomeStatsDto;
  recentActivities: HomeRecentActivityDto[];
};

export type BarnDto = {
  id: string;
  name: string;
  description: string;
  location: string;
  lat: number;
  lng: number;
  tags: string[];
  amenities: string[];
  rating: number;
  reviewCount: number;
};

export type BarnListDto = {
  items: BarnDto[];
};

export type LessonDto = {
  id: string;
  date: string;
  title: string;
  instructorName: string;
  durationMin: number;
  level: string;
  price: number;
};

export type LessonListDto = {
  items: LessonDto[];
};
