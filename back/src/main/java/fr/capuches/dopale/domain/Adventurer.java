package fr.capuches.dopale.domain;

import java.util.Objects;
import java.util.UUID;

public record Adventurer(UUID id, String name) {

	public Adventurer {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(name, "name");
		if (name.isBlank()) {
			throw new IllegalArgumentException("Le nom de l'aventurier est obligatoire");
		}
	}
}
