package com.kitnet.config;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

class DatabaseCleaner {

	private final JdbcTemplate jdbcTemplate;

	DatabaseCleaner(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	void limpar() {
		List<String> tabelas = jdbcTemplate.queryForList(
			"SELECT quote_ident(tablename) FROM pg_tables WHERE schemaname = 'public' AND tablename <> 'flyway_schema_history'",
			String.class
		);
		if (tabelas.isEmpty()) {
			return;
		}
		jdbcTemplate.execute("TRUNCATE TABLE " + String.join(", ", tabelas) + " RESTART IDENTITY CASCADE");
	}

}
