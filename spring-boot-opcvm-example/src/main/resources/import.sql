-- Users and Roles
INSERT INTO user (email, enabled, password) VALUES ('qlefevre@gmail.com', 1, '$2a$10$cb8UN7unoA3SA9lXXmIfWO/ZFA89F4/t1n7Ycnu3s5cj.8pjaNIoe');

INSERT INTO role (email, role) VALUES ('qlefevre@gmail.com', 'ROLE_USER');
INSERT INTO role (email, role) VALUES ('qlefevre@gmail.com', 'ROLE_ADMIN');

-- QuoteExtractor
INSERT INTO quote_extractor (change_group, change_regex, domain, isin_group, isin_regex, name_group, name_regex, quote_group, quote_regex) VALUES ('2', '<span class=''color@word@ variation''>@number@%</span>', 'boursorama.com', '1', '@isin@</h1>', '1', '<h1 class=''seoinline bc-system''>@any@ - @isin@</h1>', '1', '<span class="cotation">@number@EUR</span>');
INSERT INTO quote_extractor (change_group, change_regex, domain, isin_group, isin_regex, name_group, name_regex, quote_group, quote_regex) VALUES ('2', '<td><span class="@word@">@number@%</span></td>', 'opcvm360.com', '1', '<title>@isin@ - @any@ - ', '1', '<h1 id="fund-name">@any@ - @isin@</h1>', '1', '<p id="lastVL">@number@EUR');
INSERT INTO quote_extractor (change_group, change_regex, domain, isin_group, isin_regex, name_group, name_regex, quote_group, quote_regex) VALUES ('1', '<meta instrumentprop="priceChangePercent" content="@number@" />', 'lesechos.fr', '1', 'var codeisin="@isin@";', '1', '<meta instrumentprop="name" content="@namequotes@" />', '1', '<meta instrumentprop="price" content="@number@"');
INSERT INTO quote_extractor (change_group, change_regex, domain, isin_group, isin_regex, name_group, name_regex, quote_group, quote_regex) VALUES ('2', '<span id="Contenu_Contenu_Contenu_nRet1j_myLabel" class="label_@word@">@number@%', 'quantalys.com', '1', '<span id="Contenu_Contenu_Contenu_lblCodeISIN" class="label_blue">@isin@</span>', '1', '<span id="Contenu_Contenu_Contenu_lblNom" title="@namequotes@" class="label_blue">', '1', '<span id="Contenu_Contenu_Contenu_lblVLDevise" class="label_blue">@number@EUR');
INSERT INTO quote_extractor (change_group, change_regex, domain, isin_group, isin_regex, name_group, name_regex, quote_group, quote_regex) VALUES ('3', '<span class=\"h1\">@namequotes@<span class=\"@word@\">@number@%', 'boursier.com', '1', 'tc_vars\\.custom1 = ''@isin@'';', '1', '<span class=\"h1\">@namequotes@<', '1', '<td>Valeur Liquidative</td> <td class="tr">@number@€');
INSERT INTO quote_extractor (change_group, change_regex, domain, isin_group, isin_regex, name_group, name_regex, quote_group, quote_regex) VALUES ('2', '<span class="icon icon-arrow-variation-@word@"></span><span>@number@%', 'fortuneo.fr', '1', '<p class="digest-header-name-details">@isin@', '1', '<h1>@name@</h1>', '1', 'class="header-devise">@number@EUR');

-- QuoteSupplier
INSERT INTO quote_supplier (domain,enabled,start_hour) VALUES ('boursorama.com',1,7);
INSERT INTO quote_supplier (domain,enabled,start_hour) VALUES ('opcvm360.com',0,1);
INSERT INTO quote_supplier (domain,enabled,start_hour) VALUES ('quantalys.com',0,1);
INSERT INTO quote_supplier (domain,enabled,start_hour) VALUES ('lesechos.fr',1,6);
INSERT INTO quote_supplier (domain,enabled,start_hour) VALUES ('boursier.com',1,6);
INSERT INTO quote_supplier (domain,enabled,start_hour) VALUES ('fortuneo.fr',1,7);

-- Mise à jour quote et changevalue
UPDATE opcvm o JOIN ( SELECT quote, changevalue, isin FROM quote_history where date > (SELECT date(max(date)) FROM quote_history) ORDER BY date desc ) h ON o.isin = h.isin SET  o.quote = h.quote, o.changevalue = h.changevalue;

-- Liste des codes isin ayant seulement 1 url
-- SELECT COUNT(isin) AS isin_count, isin FROM  opcvm.quote_url GROUP BY isin HAVING COUNT(isin) <2 
CREATE TEMPORARY TABLE temp SELECT isin FROM  quote_url GROUP BY isin HAVING COUNT(isin) <2;
DELETE FROM  quote_url WHERE isin in (SELECT isin FROM temp);
DROP TEMPORARY TABLE temp;

-- Suppression des opcvm avec 1 url
DELETE FROM opcvm WHERE isin in (SELECT isin FROM  quote_url GROUP BY isin HAVING COUNT(isin) <2);

-- Alertes
INSERT INTO alert (changevalue,change_amount,creation_date,email,end_date,isin,max_value,min_value,modification_date,trend,type,validity_duration,value) VALUES (0,0,'2017-10-04 08:21:36','qlefevre@gmail.com','2017-12-03 08:21:36','FR0011556828',746.02,744.02,'2017-10-17 08:00:00','DOWN','TRIGGER_ALERT',60,754);
INSERT INTO alert (changevalue,change_amount,creation_date,email,end_date,isin,max_value,min_value,modification_date,trend,type,validity_duration,value) VALUES (0.01,0,'2017-10-04 08:22:14','qlefevre@gmail.com','2017-12-03 08:22:14','FR0011556828',746.02,744.02,'2017-10-17 08:00:00','DOWN','CHANGE_ALERT',60,745.02);
INSERT INTO alert (changevalue,change_amount,creation_date,email,end_date,isin,max_value,min_value,modification_date,trend,type,validity_duration,value) VALUES (0,0,'2017-10-04 08:23:35','qlefevre@gmail.com','2017-12-03 08:23:35','FR0011556828',760,744,'2017-10-17 08:00:00','UP','TUNNEL_ALERT',60,745.02);
INSERT INTO alert (changevalue,change_amount,creation_date,email,end_date,isin,max_value,min_value,modification_date,trend,type,validity_duration,value) VALUES (0.01,0,'2017-10-06 13:59:42','qlefevre@gmail.com','2017-12-05 13:59:42','FR0011631050',209.13,207.13,'2017-10-17 08:00:00','DOWN','CHANGE_ALERT',60,208.13);
INSERT INTO alert (changevalue,change_amount,creation_date,email,end_date,isin,max_value,min_value,modification_date,trend,type,validity_duration,value) VALUES (0,0,'2017-10-04 08:35:55','qlefevre@gmail.com','2017-12-03 08:35:55','FR0011631050',209.13,207.13,'2017-10-17 08:00:00','DOWN','TRIGGER_ALERT',60,212);
INSERT INTO alert (changevalue,change_amount,creation_date,email,end_date,isin,max_value,min_value,modification_date,trend,type,validity_duration,value) VALUES (0,0,'2017-10-11 12:53:08','qlefevre@gmail.com','2017-12-10 12:53:08','FR0011631050',220,210,'2017-10-17 08:00:00','UP','TUNNEL_ALERT',60,208.83);
INSERT INTO alert (changevalue,change_amount,creation_date,email,end_date,isin,max_value,min_value,modification_date,trend,type,validity_duration,value) VALUES (0,0,'2017-10-06 13:57:24','qlefevre@gmail.com','2017-12-05 13:57:24','FR0011707488',5939.23,5937.23,'2017-10-17 08:00:00','DOWN','TRIGGER_ALERT',60,5951);
INSERT INTO alert (changevalue,change_amount,creation_date,email,end_date,isin,max_value,min_value,modification_date,trend,type,validity_duration,value) VALUES (0.01,0,'2017-10-06 13:59:29','qlefevre@gmail.com','2017-12-05 13:59:29','FR0011707488',5947.71,5945.71,'2017-10-17 08:00:00','DOWN','CHANGE_ALERT',60,5946.71);
INSERT INTO alert (changevalue,change_amount,creation_date,email,end_date,isin,max_value,min_value,modification_date,trend,type,validity_duration,value) VALUES (0,0,'2017-10-11 12:53:42','qlefevre@gmail.com','2017-12-10 12:53:42','FR0011707488',6000,5958,'2017-10-17 08:00:00','UP','TUNNEL_ALERT',60,5946.71);
