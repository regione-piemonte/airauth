--
--Copyright Regione Piemonte - 2022
--SPDX-License-Identifier: EUPL-1.2-or-later
--
--
-- PostgreSQL database dump
--

-- Dumped from database version 12.2
-- Dumped by pg_dump version 12.0

-- Started on 2022-05-20 15:22:24

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 4468 (class 0 OID 17132)
-- Dependencies: 248
-- Data for Name: utenti; Type: TABLE DATA; Schema: public; Owner: dbauth
--

INSERT INTO public.utenti (id_utente, utente, password, abilitazione, data_scadenza, nome, cognome, azienda, mail, telefono, indirizzo, data_agg, autore_agg) VALUES (1, 'test', '7jl7zwIUnhnvE', true, NULL, 'test', 'test', '', '', '', '', '2022-05-20 14:21:23', 1);


--
-- TOC entry 4469 (class 0 OID 17137)
-- Dependencies: 249
-- Data for Name: ambiti; Type: TABLE DATA; Schema: public; Owner: dbauth
--

INSERT INTO public.ambiti (id_ambito, ambito, descrizione, data_agg, autore_agg) VALUES (2, 'alessandria', 'Provincia di Alessandria', '2004-04-21 09:44:48', NULL);
INSERT INTO public.ambiti (id_ambito, ambito, descrizione, data_agg, autore_agg) VALUES (3, 'asti', 'Provincia di Asti', '2004-04-21 09:44:49', NULL);
INSERT INTO public.ambiti (id_ambito, ambito, descrizione, data_agg, autore_agg) VALUES (4, 'biella', 'Provincia di Biella', '2004-04-21 09:44:49', NULL);
INSERT INTO public.ambiti (id_ambito, ambito, descrizione, data_agg, autore_agg) VALUES (5, 'cuneo', 'Provincia di Cuneo', '2004-04-21 09:44:49', NULL);
INSERT INTO public.ambiti (id_ambito, ambito, descrizione, data_agg, autore_agg) VALUES (6, 'novara-verbania', 'Province di Novara e Verbania ', '2004-04-21 09:44:49', NULL);
INSERT INTO public.ambiti (id_ambito, ambito, descrizione, data_agg, autore_agg) VALUES (7, 'torino', 'Provincia di Torino', '2004-04-21 09:44:49', NULL);
INSERT INTO public.ambiti (id_ambito, ambito, descrizione, data_agg, autore_agg) VALUES (8, 'vercelli', 'Provincia di Vercelli', '2004-04-21 09:44:49', NULL);
INSERT INTO public.ambiti (id_ambito, ambito, descrizione, data_agg, autore_agg) VALUES (11, 'piemonte', 'Regione Piemonte', '2020-06-11 09:01:58', 1);


--
-- TOC entry 4475 (class 0 OID 17164)
-- Dependencies: 255
-- Data for Name: tipo_oggetto; Type: TABLE DATA; Schema: public; Owner: dbauth
--

INSERT INTO public.tipo_oggetto (id_tipo_oggetto, descrizione, data_agg, autore_agg) VALUES (2, 'cop', '2012-10-01 11:12:58.188455', NULL);



--
-- TOC entry 4476 (class 0 OID 17169)
-- Dependencies: 256
-- Data for Name: ambiti_acl; Type: TABLE DATA; Schema: public; Owner: dbauth
--


INSERT INTO public.ambiti_acl (id_ambito, id_tipo_oggetto, id_oggetto, data_agg, autore_agg) VALUES (7, 2, '1', '2013-11-04 17:42:59', 1);
INSERT INTO public.ambiti_acl (id_ambito, id_tipo_oggetto, id_oggetto, data_agg, autore_agg) VALUES (3, 2, '2', '2013-11-04 17:43:26', 1);
INSERT INTO public.ambiti_acl (id_ambito, id_tipo_oggetto, id_oggetto, data_agg, autore_agg) VALUES (2, 2, '3', '2013-11-04 17:43:50', 1);
INSERT INTO public.ambiti_acl (id_ambito, id_tipo_oggetto, id_oggetto, data_agg, autore_agg) VALUES (5, 2, '4', '2013-11-04 17:44:17', 1);
INSERT INTO public.ambiti_acl (id_ambito, id_tipo_oggetto, id_oggetto, data_agg, autore_agg) VALUES (6, 2, '5', '2013-11-04 17:44:27', 1);
INSERT INTO public.ambiti_acl (id_ambito, id_tipo_oggetto, id_oggetto, data_agg, autore_agg) VALUES (8, 2, '6', '2013-11-04 17:44:59', 1);
INSERT INTO public.ambiti_acl (id_ambito, id_tipo_oggetto, id_oggetto, data_agg, autore_agg) VALUES (4, 2, '7', '2013-11-04 17:45:08', 1);


--
-- TOC entry 4472 (class 0 OID 17151)
-- Dependencies: 252
-- Data for Name: funzioni; Type: TABLE DATA; Schema: public; Owner: dbauth
--

INSERT INTO public.funzioni (id_funzione, funzione, descrizione, data_agg, autore_agg) VALUES (19, 'centrale', 'Utilizzo del Centrale', '2012-10-26 11:34:20', NULL);

--
-- TOC entry 4471 (class 0 OID 17146)
-- Dependencies: 251
-- Data for Name: gruppi; Type: TABLE DATA; Schema: public; Owner: dbauth
--

INSERT INTO public.gruppi (id_gruppo, gruppo, descrizione, data_agg, autore_agg) VALUES (21, 'centrale', 'Utilizzatori software Centrale', '2015-03-04 14:23:48', 1);



--
-- TOC entry 4474 (class 0 OID 17160)
-- Dependencies: 254
-- Data for Name: funzioni_gruppo; Type: TABLE DATA; Schema: public; Owner: dbauth
--


INSERT INTO public.funzioni_gruppo (id_gruppo, id_funzione, fn_scrittura, fn_avanzata, data_agg, autore_agg) VALUES (21, 19, false, false, '2015-03-04 14:36:52', 1);
INSERT


--
-- TOC entry 4473 (class 0 OID 17156)
-- Dependencies: 253
-- Data for Name: gruppi_utente; Type: TABLE DATA; Schema: public; Owner: dbauth
--

INSERT INTO public.gruppi_utente (id_utente, id_gruppo, data_agg, autore_agg) VALUES (1, 21, '2022-05-20 14:21:23', 1);


--
-- TOC entry 4483 (class 0 OID 0)
-- Dependencies: 246
-- Name: counter_ambito; Type: SEQUENCE SET; Schema: public; Owner: dbauth
--

SELECT pg_catalog.setval('public.counter_ambito', 11, true);


--
-- TOC entry 4484 (class 0 OID 0)
-- Dependencies: 245
-- Name: counter_funzione; Type: SEQUENCE SET; Schema: public; Owner: dbauth
--

SELECT pg_catalog.setval('public.counter_funzione', 20, true);


--
-- TOC entry 4485 (class 0 OID 0)
-- Dependencies: 244
-- Name: counter_gruppo; Type: SEQUENCE SET; Schema: public; Owner: dbauth
--

SELECT pg_catalog.setval('public.counter_gruppo', 36, true);


--
-- TOC entry 4486 (class 0 OID 0)
-- Dependencies: 247
-- Name: counter_tipo_oggetto; Type: SEQUENCE SET; Schema: public; Owner: dbauth
--

SELECT pg_catalog.setval('public.counter_tipo_oggetto', 4, true);


--
-- TOC entry 4487 (class 0 OID 0)
-- Dependencies: 243
-- Name: counter_utente; Type: SEQUENCE SET; Schema: public; Owner: dbauth
--

SELECT pg_catalog.setval('public.counter_utente', 2, true);


-- Completed on 2022-05-20 15:22:29

--
-- PostgreSQL database dump complete
--

