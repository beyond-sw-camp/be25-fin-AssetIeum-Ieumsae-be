package com.ieumsae.assetieum.domain.ticket.comment.entity;

import com.ieumsae.assetieum.global.common.util.KstDateTime;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ticket_comments")
public class TicketComment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ticket_comment_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ticket_id", nullable = false)
	private Ticket ticket;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "writer_id", nullable = false)
	private Member writer;

	@Column(nullable = false, length = 255)
	private String content;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public static TicketComment create(Company company, Ticket ticket, Member writer, String content) {
		return TicketComment.builder()
			.company(company)
			.ticket(ticket)
			.writer(writer)
			.content(content)
			.build();
	}

	public void updateContent(String content) {
		this.content = content;
	}

	public LocalDateTime delete() {
		this.deletedAt = KstDateTime.now();
		return this.deletedAt;
	}
}
