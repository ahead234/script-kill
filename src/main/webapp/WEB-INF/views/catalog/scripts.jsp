<%@ include file="../common/header.jspf" %>
<section class="slider" data-slider data-interval="4500" aria-label="剧场推荐">
    <div class="slider-track" data-slider-track>
        <div class="slide is-active"><img src="${ctx}/assets/img/banner-1.jpg" alt="剧场座椅与灯光"></div>
        <div class="slide"><img src="${ctx}/assets/img/banner-2.jpg" alt="雾港与信封"></div>
        <div class="slide"><img src="${ctx}/assets/img/banner-3.jpg" alt="夜行列车与时刻表"></div>
    </div>
    <div class="slider-dots" data-slider-dots>
        <button class="dot is-active" type="button" data-slide-to="0" aria-label="查看第 1 张"></button>
        <button class="dot" type="button" data-slide-to="1" aria-label="查看第 2 张"></button>
        <button class="dot" type="button" data-slide-to="2" aria-label="查看第 3 张"></button>
    </div>
</section>

<section class="section-block">
    <h2 class="section-title">从题材、人数与时长出发，找到适合你的剧本。</h2>
    <%@ include file="../common/messages.jspf" %>
    <form class="filter-bar" method="get" action="${ctx}/scripts">
        <div class="field field-grow">
            <label for="keyword">剧本名称</label>
            <input class="input" id="keyword" name="keyword" maxlength="120" placeholder="输入剧本名称" value="<c:out value='${param.keyword}'/>">
        </div>
        <div class="field">
            <label for="genre">题材</label>
            <select class="select" id="genre" name="genre">
                <option value="">全部题材</option>
                <c:forEach items="${['推理','情感','欢乐','悬疑','恐怖','机制','阵营']}" var="genre">
                    <option value="${genre}" ${param.genre == genre ? 'selected' : ''}><c:out value="${genre}"/></option>
                </c:forEach>
            </select>
        </div>
        <button class="button button-primary" type="submit">查找</button>
    </form>

    <c:choose>
        <c:when test="${empty result.items}">
            <div class="empty-state"><strong>没有找到符合条件的剧本</strong>换一个关键词或题材试试。</div>
        </c:when>
        <c:otherwise>
            <div class="script-grid">
                <c:forEach items="${result.items}" var="book">
                    <c:set var="difficultyText" value="${book.difficulty == 'EASY' ? '简单' : (book.difficulty == 'HARD' ? '困难' : '中等')}"/>
                    <article class="card script-card">
                        <img class="script-cover-img" src="${ctx}${empty book.coverPath ? '/assets/img/cover-1.svg' : book.coverPath}" alt="<c:out value='${book.title}'/> 封面">
                        <div class="script-card-body">
                            <h2 class="script-title"><c:out value="${book.title}"/></h2>
                            <div class="script-meta">
                                <span>${book.minPlayers}–${book.maxPlayers} 人</span>
                                <span><c:out value="${book.genre}"/></span>
                                <span><c:out value="${difficultyText}"/></span>
                                <span>${book.durationMinutes} 分钟</span>
                            </div>
                            <p class="script-desc"><c:out value="${book.description}"/></p>
                            <div class="script-card-footer">
                                <span class="rating">
                                    <c:choose>
                                        <c:when test="${book.reviewCount > 0}">${book.averageRating} 分</c:when>
                                        <c:otherwise>暂无评分</c:otherwise>
                                    </c:choose>
                                </span>
                                <a class="button button-small button-primary" href="${ctx}/scripts/detail?id=${book.id}">查看场次</a>
                            </div>
                        </div>
                    </article>
                </c:forEach>
            </div>
            <%@ include file="../common/pagination.jspf" %>
        </c:otherwise>
    </c:choose>
</section>
<%@ include file="../common/footer.jspf" %>
