package io.volvox.chats;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.sql.Date;
import java.util.StringJoiner;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity(name = "chat_username")
@Cacheable
public class HistoricChatUsername extends PanacheEntityBase implements Comparable<HistoricChatUsername> {
	@Id
	@GeneratedValue
	public Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "chat_id", referencedColumnName = "id", nullable = true)
	Chat chat;

	// Field definition and bounds
	@Column(name = "time", nullable = false)
	public Date time;

	// Field definition and bounds
	@Size(message = "Username length is not valid", min = 5)
	@Column(length = 48)
	@Size(message = "Username must not be an empty string", min = 1, max = 12 + 32)
	@Pattern(message = "Username contains invalid characters", regexp = "^(?:[a-zA-Z\\d][_]?)+$")
	@Pattern(message = "Username is not valid", regexp = "^(?:translation_|mv_)?[a-zA-Z]([a-zA-Z_\\d]){1,30}[a-zA-Z\\d]$")
	// Keep field history
	public String username;

	public static Uni<HistoricChatUsername> findNewest(Long chatId) {
		if (chatId == null) {
			throw new NullPointerException("Id must not be null");
		}
		return find("from chat_username where chat_id = ?1", Sort.by("time").descending(), chatId).firstResult();
	}

	public static Multi<HistoricChatUsername> listAll(Long chatId) {
		if (chatId == null) {
			throw new NullPointerException("Id must not be null");
		}
		return find("from chat_username where chat_id = ?1", Sort.by("time").ascending(), chatId).stream();
	}

	public static Uni<Long> deleteByChatId(Long chatId) {
		return delete("from chat_username where chat_id = ?1", chatId);
	}

	@Override public int compareTo(HistoricChatUsername o) {
		return o.time.compareTo(this.time);
	}

	@Override public String toString() {
		return new StringJoiner(", ", HistoricChatUsername.class.getSimpleName() + "[", "]")
			.add("id=" + id)
			.add("time=" + time)
			.add("username='" + username + "'")
			.toString();
	}
}
