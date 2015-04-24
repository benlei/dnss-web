require_relative 'common'

conn = createPGConn()
query = <<sql_query
  INSERT INTO skills_assassin_%1$s
    SELECT *
    FROM skills_assassin_bringer_%1$s
    WHERE _id NOT IN (
      SELECT _id
      FROM skills_assassin_%1$s
    )
sql_query
conn.exec(query % 'pve')
conn.exec(query % 'pvp')
conn.close()