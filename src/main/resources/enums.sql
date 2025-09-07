

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
