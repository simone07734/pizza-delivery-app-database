#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
#cs166_createdb $USER"_project_phase_3_DB"
cs166_psql -p $PGPORT $USER"_project_phase_3_DB" < $DIR/../src/create_tables.sql
cs166_psql -p $PGPORT $USER"_project_phase_3_DB" < $DIR/../src/create_indexes.sql
cs166_psql -p $PGPORT $USER"_project_phase_3_DB" < $DIR/../src/load_data.sql

