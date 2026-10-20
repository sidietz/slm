

-- Fiction:
-- Fantasy, Science Fiction, Action&Adventure, Crime, Horror, Thriller, Historical Fiction, Romance
-- GraphicNovel, ShortStory, YoungAdult, NewAdult, Cildren,  (Dystopian, LGBTQ, Women's Fiction)
-- Non-fiction:
-- Biography, Food&Drink, Photography, SelfHelp, History, Travel, TrueCrime, Humor, Essays, Guide, Religion,
-- Humanities, Parenting, Science, 

CREATE TYPE book_genre AS ENUM ('ACTION', 'BIOGRAPHY', 'CHILDREN', 'CRIME', 'ESSAY', 'FANTASY',
'FOODDRINK', 'GRAPHICNOVEL', 'GUIDE', 'HISTORICALFICTION', 'HISTORY', 'HORROR' ,'HUMANITIES',
'HUMOR', 'IT', 'NEWADULT', 'PARENTING', 'PHILOSOPHICFICTION', 'PHOTOGRAPHY', 'RELIGION', 'ROMANCE',
'SCIENCE', 'SCIFI', 'SELFHELP', 'SHORTSTORY', 'THRILLER', 'TRAVEL', 'TRUECRIME', 'YOUNGADULT');

CREATE TYPE book_type AS ENUM ('FICTION', 'NONFICTION');

CREATE TYPE source_type AS ENUM ('BOOK', 'COURSE', 'LECTURE', 'TUTORIAL', 'YOUTUBE');
CREATE TYPE status_type AS ENUM('PLANNED', 'IN_PROGRESS', 'COMPLETED');

CREATE TYPE human_category AS ENUM ('FAMILY', 'CLASSMATE', 'FRIEND', 'FRIEND_OF_FRIEND', 'TEACHER', 'LECTURER', 'CELEBRITY', 'POLITICIAN', 'SPD', 'OTHER');
CREATE TYPE event_type AS ENUM ('BIRTHDAY', 'MEMORIAL', 'WEDDING_ANNIVERSARY', 'RELATIONSHIP_ANNIVERSARY', 'WORK_ANNIVERSARY', 'CUSTOM');

CREATE TYPE fuel_type AS ENUM (
    'DIESEL',
    'SUPER_E10',
    'SUPER_E5',
    'SUPER_PLUS',
    'PREMIUM_DIESEL',
    'LPG',
    'CNG',
    'ELECTRIC',
    'HYDROGEN',
    'OTHER'
);

CREATE TYPE fueling_type AS ENUM (
    'FULL',
    'PARTIAL',
    'INITIAL'
);

CREATE TYPE driving_style AS ENUM (
    'ECONOMICAL',
    'NORMAL',
    'FAST'
);

CREATE TYPE tire_type AS ENUM (
    'SUMMER',
    'WINTER',
    'ALL_SEASON'
);

CREATE TYPE station_type AS ENUM (
    'GAS_STATION',
    'CHARGING_STATION',
    'H2_STATION',
    'OTHER'
);

CREATE TYPE plug_type AS ENUM (
    'CCS_TYPE2',
    'CHADEMO',
    'GBT',
    'OTHER',
    'TESLA',
    'TYPE_1',
    'TYPE_2',
    'UNKNOWN'
);

