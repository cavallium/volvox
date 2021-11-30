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

@Entity(name = "chat_name")
@Cacheable
public class HistoricChatName extends PanacheEntityBase implements Comparable<HistoricChatName> {
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
	@Column(length = 128)
	public String name;

	public static Uni<HistoricChatName> findNewest(Long chatId) {
		if (chatId == null) {
			throw new NullPointerException("Id must not be null");
		}
		return find("from chat_name where chat_id = ?1", Sort.by("time").descending(), chatId).firstResult();
	}

	public static Multi<HistoricChatName> listAll(Long chatId) {
		if (chatId == null) {
			throw new NullPointerException("Id must not be null");
		}
		return find("from chat_name where chat_id = ?1", Sort.by("time").ascending(), chatId).stream();
	}

	public static Uni<Long> deleteByChatId(Long chatId) {
		return delete("from chat_name where chat_id = ?1", chatId);
	}

	@Override public int compareTo(HistoricChatName o) {
		return o.time.compareTo(this.time);
	}

	@Override public String toString() {
		return new StringJoiner(", ", HistoricChatName.class.getSimpleName() + "[", "]")
			.add("id=" + id)
			.add("time=" + time)
			.add("name='" + name + "'")
			.toString();
	}
}
