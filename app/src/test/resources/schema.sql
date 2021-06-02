ALTER TABLE IF EXISTS document DROP COLUMN IF EXISTS signature_model_json;
ALTER TABLE IF EXISTS document DROP COLUMN IF EXISTS template_model_json;

CREATE TABLE IF NOT EXISTS signature_record (
	id varchar(50) primary key,
	signature_hex varchar(1024) not null,
	signature_hex_algorithm varchar(255) not null,
	signature_algorithm varchar(255) not null,
	key_id varchar(255) not null,
	key_region varchar(255) not null
);