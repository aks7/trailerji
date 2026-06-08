ALTER TABLE channel_configs ADD COLUMN category VARCHAR(255);

ALTER TABLE cached_trailers ADD COLUMN channel_id VARCHAR(255);

UPDATE channel_configs SET category = 'hollywood_movie_trailer' WHERE channel_name IN (
    'Marvel Entertainment',
    'Warner Bros. Pictures',
    'Universal Pictures',
    'Sony Pictures Entertainment',
    'Paramount Pictures',
    '20th Century Studios',
    'Disney',
    'Paramount Movies UK',
    'Sky Cinema',
    'Universal Movies International',
    'Sony',
    'A24',
    'Lionsgate Movies',
    'Netflix',
    'HBO',
    'Apple TV',
    'Amazon MGM Studios',
    'Searchlight Pictures',
    'Focus Features',
    'Illumination',
    'DreamWorks Animation',
    'Walt Disney Animation Studios',
    'Pixar'
);

UPDATE channel_configs SET category = 'bollywood_movie_trailer' WHERE channel_name IN (
    'T-Series',
    'YRF - Yash Raj Films',
    'Zee Studios',
    'Warner Bros. Pictures India',
    'Sony Pictures India',
    'Eros Now Music',
    'Red Chillies Entertainment',
    'Dharma Productions',
    'UTV Motion Pictures',
    'Reliance Entertainment',
    'Pen Movies',
    'BalajiMotionPictures',
    'Tips Official',
    'Saregama Music',
    'Venus Movies',
    'PVR Pictures',
    'Maddock Films',
    'Excel Movies',
    'SVF'
);
