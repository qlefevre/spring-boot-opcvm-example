/*******************************************************************************
 * Copyright 2018  Quentin Lefèvre
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.github.qlefevre.opcvm.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.github.qlefevre.opcvm.domain.QuoteUrl;

@Transactional
public interface QuoteUrlRepository extends PagingAndSortingRepository<QuoteUrl, Long> {

	List<QuoteUrl> findQuoteUrlsByDomainInAndIsinInOrderByIsinAscDomainAsc(Collection<String> domain,
			Collection<String> isin);

	List<QuoteUrl> findQuoteUrlsByDomainInOrderByIsinAscDomainAsc(Collection<String> domain);
	
	@Query("select u from QuoteUrl u where u.domain = :domain and "
			+ " u.isin not in (select r.isin from QuoteResult r where r.url like :domainlike)")
	List<QuoteUrl> findQuoteUrlsByDomainOrderByIsinAscDomainAsc(@Param("domain") String domain,@Param("domainlike") String domainlike);

}