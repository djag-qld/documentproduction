create table if not exists api_key (id varchar(255) not null, created timestamp not null, created_by varchar(255) not null, last_modified timestamp not null, last_modified_by varchar(255) not null, agency varchar(255) not null, api_key_hash varchar(255) not null, api_key_id varchar(255) not null, enabled boolean, last_used timestamp, primary key (id));
create table if not exists audit_action (id varchar(255) not null, created timestamp not null, created_by varchar(255) not null, last_modified timestamp not null, last_modified_by varchar(255) not null, agency varchar(255) not null, event varchar(255) not null, target varchar(255) not null, target_id varchar(255), target_type varchar(255) not null, primary key (id));
create table if not exists document (id varchar(255) not null, created timestamp not null, created_by varchar(255) not null, last_modified timestamp not null, last_modified_by varchar(255) not null, agency varchar(255) not null, counter bigint, template_id varchar(255), primary key (id));
create table if not exists document_signatures (document_id varchar(255) not null, signatures_id varchar(255) not null);
create table if not exists document_signature (id varchar(255) not null, created timestamp not null, created_by varchar(255) not null, last_modified timestamp not null, last_modified_by varchar(255) not null, agency varchar(255) not null, alias varchar(255) not null, latest boolean not null, reason_template varchar(255), signatory_template varchar(255), version integer not null, signature_key_id varchar(255) not null, primary key (id));
create table if not exists signature_key (id varchar(255) not null, created timestamp not null, created_by varchar(255) not null, last_modified timestamp not null, last_modified_by varchar(255) not null, agency varchar(255) not null, alias varchar(255) not null, certificate varchar(10000) not null, kms_id varchar(255) not null, latest boolean not null, timestamp_endpoint varchar(255), version integer not null, primary key (id));
create table if not exists template (id varchar(255) not null, created timestamp not null, created_by varchar(255) not null, last_modified timestamp not null, last_modified_by varchar(255) not null, agency varchar(255) not null, alias varchar(255) not null, content varchar(100000) not null, latest boolean not null, version integer not null, primary key (id));
alter table api_key drop constraint if exists UK_5p7h0wlmxg4m319gfc99g39n0;
alter table api_key add constraint UK_5p7h0wlmxg4m319gfc99g39n0 unique (api_key_id);

alter table document drop constraint if exists FKeijg5cw28im526wmdis62lyi2;
alter table document add constraint FKeijg5cw28im526wmdis62lyi2 foreign key (template_id) references template;

alter table document_signatures drop constraint if exists FKooqj1wvo93d3sdgrsrdod8eyo;
alter table document_signatures add constraint FKooqj1wvo93d3sdgrsrdod8eyo foreign key (signatures_id) references document_signature;

alter table document_signatures drop constraint if exists FKkyghmnpnlj6uvp4jcchejc2yh;
alter table document_signatures add constraint FKkyghmnpnlj6uvp4jcchejc2yh foreign key (document_id) references document;

alter table document_signature drop constraint if exists FKqhoc52qlvn0ba1qv3wf2ist7r;
alter table document_signature add constraint FKqhoc52qlvn0ba1qv3wf2ist7r foreign key (signature_key_id) references signature_key;

ALTER TABLE IF EXISTS document DROP COLUMN IF EXISTS signature_model_json;
ALTER TABLE IF EXISTS document DROP COLUMN IF EXISTS template_model_json;
ALTER TABLE IF EXISTS signature_key ALTER COLUMN certificate TYPE TEXT;
ALTER TABLE IF EXISTS template ALTER COLUMN content TYPE TEXT;
ALTER TABLE IF EXISTS document ADD COLUMN IF NOT EXISTS counter BIGINT DEFAULT 0;
CREATE SEQUENCE IF NOT EXISTS DOCUMENT_COUNTER_SEQ;

CREATE TABLE IF NOT EXISTS DOCUMENT_COUNTER (
	ID BIGINT PRIMARY KEY
);

ALTER TABLE IF EXISTS signature_key ADD COLUMN IF NOT EXISTS timestamp_endpoint TEXT;

ALTER TABLE document_signature ADD COLUMN IF NOT EXISTS location_template TEXT;
ALTER TABLE document_signature ADD COLUMN IF NOT EXISTS contact_info_template TEXT;

CREATE TABLE IF NOT EXISTS signature_record (
	id varchar(50) primary key,
	signature_hex varchar(1024) not null,
	signature_hex_algorithm varchar(255) not null,
	signature_algorithm varchar(255) not null,
	key_id varchar(255) not null,
	key_region varchar(255) not null
);

ALTER TABLE signature_record ADD COLUMN IF NOT EXISTS created_at timestamp not null DEFAULT now();
ALTER TABLE signature_record ADD COLUMN IF NOT EXISTS last_modified_at timestamp not null DEFAULT now();
ALTER TABLE signature_record ADD COLUMN IF NOT EXISTS status text not null DEFAULT 'VALID';