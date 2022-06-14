--
--Copyright Regione Piemonte - 2022
--SPDX-License-Identifier: EUPL-1.2-or-later
--
--
-- PostgreSQL database dump
--

-- Dumped from database version 12.2
-- Dumped by pg_dump version 12.0

-- Started on 2022-05-20 11:42:32

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

SET SESSION AUTHORIZATION 'postgres';

--
-- TOC entry 4468 (class 1262 OID 16385)
-- Name: dbauth2; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE dbauth2 WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8';


\connect dbauth2

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

SET SESSION AUTHORIZATION 'postgres';

--
-- TOC entry 4469 (class 0 OID 0)
-- Name: dbauth2; Type: DATABASE PROPERTIES; Schema: -; Owner: postgres
--

ALTER DATABASE dbauth2 SET search_path TO '$user', 'public', 'topology';


\connect dbauth2

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

SET SESSION AUTHORIZATION 'postgres';

--
-- TOC entry 10 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA public;


--
-- TOC entry 4470 (class 0 OID 0)
-- Dependencies: 10
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS 'standard public schema';


SET SESSION AUTHORIZATION 'dbauth';

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 249 (class 1259 OID 17137)
-- Name: ambiti; Type: TABLE; Schema: public; Owner: dbauth
--

CREATE TABLE public.ambiti (
    id_ambito integer DEFAULT nextval(('counter_ambito'::text)::regclass) NOT NULL,
    ambito character varying(32) NOT NULL,
    descrizione character varying(128),
    data_agg timestamp without time zone DEFAULT ('now'::text)::timestamp(0) with time zone NOT NULL,
    autore_agg integer
);


--
-- TOC entry 256 (class 1259 OID 17169)
-- Name: ambiti_acl; Type: TABLE; Schema: public; Owner: dbauth
--

CREATE TABLE public.ambiti_acl (
    id_ambito integer DEFAULT 1 NOT NULL,
    id_tipo_oggetto integer NOT NULL,
    id_oggetto character varying(32) NOT NULL,
    data_agg timestamp without time zone DEFAULT ('now'::text)::timestamp(0) with time zone NOT NULL,
    autore_agg integer
);


--
-- TOC entry 250 (class 1259 OID 17142)
-- Name: ambiti_utenti; Type: TABLE; Schema: public; Owner: dbauth
--

CREATE TABLE public.ambiti_utenti (
    id_ambito integer NOT NULL,
    id_utente integer NOT NULL,
    data_agg timestamp without time zone DEFAULT ('now'::text)::timestamp(0) with time zone NOT NULL,
    autore_agg integer
);


--
-- TOC entry 246 (class 1259 OID 17128)
-- Name: counter_ambito; Type: SEQUENCE; Schema: public; Owner: dbauth
--

CREATE SEQUENCE public.counter_ambito
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 245 (class 1259 OID 17126)
-- Name: counter_funzione; Type: SEQUENCE; Schema: public; Owner: dbauth
--

CREATE SEQUENCE public.counter_funzione
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 244 (class 1259 OID 17124)
-- Name: counter_gruppo; Type: SEQUENCE; Schema: public; Owner: dbauth
--

CREATE SEQUENCE public.counter_gruppo
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 247 (class 1259 OID 17130)
-- Name: counter_tipo_oggetto; Type: SEQUENCE; Schema: public; Owner: dbauth
--

CREATE SEQUENCE public.counter_tipo_oggetto
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 243 (class 1259 OID 17122)
-- Name: counter_utente; Type: SEQUENCE; Schema: public; Owner: dbauth
--

CREATE SEQUENCE public.counter_utente
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 252 (class 1259 OID 17151)
-- Name: funzioni; Type: TABLE; Schema: public; Owner: dbauth
--

CREATE TABLE public.funzioni (
    id_funzione integer DEFAULT nextval(('counter_funzione'::text)::regclass) NOT NULL,
    funzione character varying(32) NOT NULL,
    descrizione character varying(128),
    data_agg timestamp without time zone DEFAULT ('now'::text)::timestamp(0) with time zone NOT NULL,
    autore_agg integer
);


--
-- TOC entry 257 (class 1259 OID 17174)
-- Name: funzioni_gruppi_ambiti; Type: TABLE; Schema: public; Owner: dbauth
--

CREATE TABLE public.funzioni_gruppi_ambiti (
    id_funzione integer NOT NULL,
    id_gruppo integer NOT NULL,
    id_ambito integer NOT NULL
);


--
-- TOC entry 254 (class 1259 OID 17160)
-- Name: funzioni_gruppo; Type: TABLE; Schema: public; Owner: dbauth
--

CREATE TABLE public.funzioni_gruppo (
    id_gruppo integer NOT NULL,
    id_funzione integer NOT NULL,
    fn_scrittura boolean NOT NULL,
    fn_avanzata boolean NOT NULL,
    data_agg timestamp without time zone DEFAULT ('now'::text)::timestamp(0) with time zone NOT NULL,
    autore_agg integer
);


--
-- TOC entry 251 (class 1259 OID 17146)
-- Name: gruppi; Type: TABLE; Schema: public; Owner: dbauth
--

CREATE TABLE public.gruppi (
    id_gruppo integer DEFAULT nextval(('counter_gruppo'::text)::regclass) NOT NULL,
    gruppo character varying(32) NOT NULL,
    descrizione character varying(128),
    data_agg timestamp without time zone DEFAULT ('now'::text)::timestamp(0) with time zone NOT NULL,
    autore_agg integer
);


--
-- TOC entry 253 (class 1259 OID 17156)
-- Name: gruppi_utente; Type: TABLE; Schema: public; Owner: dbauth
--

CREATE TABLE public.gruppi_utente (
    id_utente integer NOT NULL,
    id_gruppo integer NOT NULL,
    data_agg timestamp without time zone DEFAULT ('now'::text)::timestamp(0) with time zone NOT NULL,
    autore_agg integer
);


--
-- TOC entry 255 (class 1259 OID 17164)
-- Name: tipo_oggetto; Type: TABLE; Schema: public; Owner: dbauth
--

CREATE TABLE public.tipo_oggetto (
    id_tipo_oggetto integer DEFAULT nextval(('counter_tipo_oggetto'::text)::regclass) NOT NULL,
    descrizione character varying(50),
    data_agg timestamp without time zone DEFAULT ('now'::text)::timestamp(0) with time zone NOT NULL,
    autore_agg integer
);


--
-- TOC entry 248 (class 1259 OID 17132)
-- Name: utenti; Type: TABLE; Schema: public; Owner: dbauth
--

CREATE TABLE public.utenti (
    id_utente integer DEFAULT nextval(('counter_utente'::text)::regclass) NOT NULL,
    utente character varying(32) NOT NULL,
    password character varying(40) NOT NULL,
    abilitazione boolean NOT NULL,
    data_scadenza date,
    nome character varying(32),
    cognome character varying(32),
    azienda character varying(32),
    mail character varying(64),
    telefono character varying(32),
    indirizzo character varying(64),
    data_agg timestamp without time zone DEFAULT ('now'::text)::timestamp(0) with time zone NOT NULL,
    autore_agg integer
);


--
-- TOC entry 4300 (class 2606 OID 17204)
-- Name: ambiti_acl ambiti_acl_pkey; Type: CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.ambiti_acl
    ADD CONSTRAINT ambiti_acl_pkey PRIMARY KEY (id_ambito, id_tipo_oggetto, id_oggetto);


--
-- TOC entry 4278 (class 2606 OID 17186)
-- Name: ambiti ambiti_ambito_key; Type: CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.ambiti
    ADD CONSTRAINT ambiti_ambito_key UNIQUE (ambito);


--
-- TOC entry 4280 (class 2606 OID 17184)
-- Name: ambiti ambiti_pkey; Type: CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.ambiti
    ADD CONSTRAINT ambiti_pkey PRIMARY KEY (id_ambito);


--
-- TOC entry 4282 (class 2606 OID 17188)
-- Name: ambiti_utenti ambiti_utenti_pkey; Type: CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.ambiti_utenti
    ADD CONSTRAINT ambiti_utenti_pkey PRIMARY KEY (id_ambito, id_utente);


--
-- TOC entry 4290 (class 2606 OID 17198)
-- Name: funzioni funzioni_funzione_key; Type: CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.funzioni
    ADD CONSTRAINT funzioni_funzione_key UNIQUE (funzione);


--
-- TOC entry 4302 (class 2606 OID 17206)
-- Name: funzioni_gruppi_ambiti funzioni_gruppi_ambiti_pkey; Type: CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.funzioni_gruppi_ambiti
    ADD CONSTRAINT funzioni_gruppi_ambiti_pkey PRIMARY KEY (id_funzione, id_gruppo, id_ambito);


--
-- TOC entry 4296 (class 2606 OID 17208)
-- Name: funzioni_gruppo funzioni_gruppo_pkey; Type: CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.funzioni_gruppo
    ADD CONSTRAINT funzioni_gruppo_pkey PRIMARY KEY (id_gruppo, id_funzione);


--
-- TOC entry 4292 (class 2606 OID 17196)
-- Name: funzioni funzioni_pkey; Type: CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.funzioni
    ADD CONSTRAINT funzioni_pkey PRIMARY KEY (id_funzione);


--
-- TOC entry 4284 (class 2606 OID 17194)
-- Name: gruppi gruppi_gruppo_key; Type: CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.gruppi
    ADD CONSTRAINT gruppi_gruppo_key UNIQUE (gruppo);


--
-- TOC entry 4286 (class 2606 OID 17192)
-- Name: gruppi gruppi_id_gruppo_key; Type: CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.gruppi
    ADD CONSTRAINT gruppi_id_gruppo_key UNIQUE (id_gruppo);


--
-- TOC entry 4288 (class 2606 OID 17190)
-- Name: gruppi gruppi_pkey; Type: CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.gruppi
    ADD CONSTRAINT gruppi_pkey PRIMARY KEY (id_gruppo, gruppo);


--
-- TOC entry 4294 (class 2606 OID 17200)
-- Name: gruppi_utente gruppi_utente_pkey; Type: CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.gruppi_utente
    ADD CONSTRAINT gruppi_utente_pkey PRIMARY KEY (id_utente, id_gruppo);


--
-- TOC entry 4298 (class 2606 OID 17202)
-- Name: tipo_oggetto tipo_oggetto_pkey; Type: CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.tipo_oggetto
    ADD CONSTRAINT tipo_oggetto_pkey PRIMARY KEY (id_tipo_oggetto);


--
-- TOC entry 4272 (class 2606 OID 17180)
-- Name: utenti utenti_id_utente_key; Type: CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.utenti
    ADD CONSTRAINT utenti_id_utente_key UNIQUE (id_utente);


--
-- TOC entry 4274 (class 2606 OID 17178)
-- Name: utenti utenti_pkey; Type: CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.utenti
    ADD CONSTRAINT utenti_pkey PRIMARY KEY (id_utente, utente);


--
-- TOC entry 4276 (class 2606 OID 17182)
-- Name: utenti utenti_utente_key; Type: CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.utenti
    ADD CONSTRAINT utenti_utente_key UNIQUE (utente);


--
-- TOC entry 4303 (class 2606 OID 17209)
-- Name: ambiti $1; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.ambiti
    ADD CONSTRAINT "$1" FOREIGN KEY (autore_agg) REFERENCES public.utenti(id_utente) ON DELETE SET NULL;


--
-- TOC entry 4304 (class 2606 OID 17214)
-- Name: ambiti_utenti $1; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.ambiti_utenti
    ADD CONSTRAINT "$1" FOREIGN KEY (id_ambito) REFERENCES public.ambiti(id_ambito) ON UPDATE CASCADE ON DELETE SET DEFAULT;


--
-- TOC entry 4307 (class 2606 OID 17229)
-- Name: gruppi $1; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.gruppi
    ADD CONSTRAINT "$1" FOREIGN KEY (autore_agg) REFERENCES public.utenti(id_utente) ON DELETE SET NULL;


--
-- TOC entry 4308 (class 2606 OID 17234)
-- Name: funzioni $1; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.funzioni
    ADD CONSTRAINT "$1" FOREIGN KEY (autore_agg) REFERENCES public.utenti(id_utente) ON DELETE SET NULL;


--
-- TOC entry 4309 (class 2606 OID 17239)
-- Name: gruppi_utente $1; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.gruppi_utente
    ADD CONSTRAINT "$1" FOREIGN KEY (id_utente) REFERENCES public.utenti(id_utente) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 4312 (class 2606 OID 17254)
-- Name: funzioni_gruppo $1; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.funzioni_gruppo
    ADD CONSTRAINT "$1" FOREIGN KEY (id_gruppo) REFERENCES public.gruppi(id_gruppo) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 4315 (class 2606 OID 17269)
-- Name: tipo_oggetto $1; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.tipo_oggetto
    ADD CONSTRAINT "$1" FOREIGN KEY (autore_agg) REFERENCES public.utenti(id_utente) ON DELETE SET NULL;


--
-- TOC entry 4316 (class 2606 OID 17274)
-- Name: ambiti_acl $1; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.ambiti_acl
    ADD CONSTRAINT "$1" FOREIGN KEY (id_ambito) REFERENCES public.ambiti(id_ambito) ON UPDATE CASCADE ON DELETE SET DEFAULT;


--
-- TOC entry 4319 (class 2606 OID 17289)
-- Name: funzioni_gruppi_ambiti $1; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.funzioni_gruppi_ambiti
    ADD CONSTRAINT "$1" FOREIGN KEY (id_funzione) REFERENCES public.funzioni(id_funzione);


--
-- TOC entry 4305 (class 2606 OID 17219)
-- Name: ambiti_utenti $2; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.ambiti_utenti
    ADD CONSTRAINT "$2" FOREIGN KEY (id_utente) REFERENCES public.utenti(id_utente) ON DELETE SET NULL;


--
-- TOC entry 4310 (class 2606 OID 17244)
-- Name: gruppi_utente $2; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.gruppi_utente
    ADD CONSTRAINT "$2" FOREIGN KEY (id_gruppo) REFERENCES public.gruppi(id_gruppo) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 4313 (class 2606 OID 17259)
-- Name: funzioni_gruppo $2; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.funzioni_gruppo
    ADD CONSTRAINT "$2" FOREIGN KEY (id_funzione) REFERENCES public.funzioni(id_funzione) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 4317 (class 2606 OID 17279)
-- Name: ambiti_acl $2; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.ambiti_acl
    ADD CONSTRAINT "$2" FOREIGN KEY (id_tipo_oggetto) REFERENCES public.tipo_oggetto(id_tipo_oggetto);


--
-- TOC entry 4320 (class 2606 OID 17294)
-- Name: funzioni_gruppi_ambiti $2; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.funzioni_gruppi_ambiti
    ADD CONSTRAINT "$2" FOREIGN KEY (id_gruppo) REFERENCES public.gruppi(id_gruppo);


--
-- TOC entry 4306 (class 2606 OID 17224)
-- Name: ambiti_utenti $3; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.ambiti_utenti
    ADD CONSTRAINT "$3" FOREIGN KEY (autore_agg) REFERENCES public.utenti(id_utente) ON DELETE SET NULL;


--
-- TOC entry 4311 (class 2606 OID 17249)
-- Name: gruppi_utente $3; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.gruppi_utente
    ADD CONSTRAINT "$3" FOREIGN KEY (autore_agg) REFERENCES public.utenti(id_utente) ON DELETE SET NULL;


--
-- TOC entry 4318 (class 2606 OID 17284)
-- Name: ambiti_acl $3; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.ambiti_acl
    ADD CONSTRAINT "$3" FOREIGN KEY (autore_agg) REFERENCES public.utenti(id_utente) ON DELETE SET NULL;


--
-- TOC entry 4321 (class 2606 OID 17299)
-- Name: funzioni_gruppi_ambiti $3; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.funzioni_gruppi_ambiti
    ADD CONSTRAINT "$3" FOREIGN KEY (id_ambito) REFERENCES public.ambiti(id_ambito);


--
-- TOC entry 4314 (class 2606 OID 17264)
-- Name: funzioni_gruppo $4; Type: FK CONSTRAINT; Schema: public; Owner: dbauth
--

ALTER TABLE ONLY public.funzioni_gruppo
    ADD CONSTRAINT "$4" FOREIGN KEY (autore_agg) REFERENCES public.utenti(id_utente) ON DELETE SET NULL;


-- Completed on 2022-05-20 11:42:35

--
-- PostgreSQL database dump complete
--

