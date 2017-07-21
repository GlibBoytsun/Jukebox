CREATE TABLE data (
  userID int(11) DEFAULT NULL,
  groupID int(11) DEFAULT NULL,
  dateEnd datetime DEFAULT NULL,
  songs text,
  locationHistory text,
  time int(11) DEFAULT NULL,
  distance int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE groups (
  id int(11) DEFAULT NULL,
  name varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE playlists (
  id int(11) NOT NULL AUTO_INCREMENT,
  groupid int(11) DEFAULT NULL,
  nextSongIndex int(11) DEFAULT NULL,
  nextSongStartTime datetime DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;

CREATE TABLE playlistSongs (
  playlistID int(11) DEFAULT NULL,
  songID int(11) DEFAULT NULL,
  songIndex int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE songs (
  id int(11) NOT NULL AUTO_INCREMENT,
  title varchar(100) DEFAULT NULL,
  artist varchar(100) DEFAULT NULL,
  duration int(11) DEFAULT NULL,
  URL varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=latin1;

CREATE TABLE users (
  ID int(11) NOT NULL AUTO_INCREMENT,
  Name varchar(45) NOT NULL,
  UID varchar(45) DEFAULT NULL,
  PRIMARY KEY (ID),
  UNIQUE KEY ID_UNIQUE (ID)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;