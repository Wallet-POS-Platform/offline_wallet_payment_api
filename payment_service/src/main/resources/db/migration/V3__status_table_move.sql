-- Remove Status Column from Clients and Merchants and Added into User table
alter table payment_service.merchants drop column status;
alter table payment_service.clients drop column status;

alter table payment_service.users add status text not null default 'OK';