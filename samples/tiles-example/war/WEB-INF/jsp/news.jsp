<%@taglib uri="http://jakarta.apache.org/tiles" prefix="tiles" %>
<%@taglib uri="http://java.sun.com/jstl/c" prefix="c" %>

<tiles:importAttribute name="sourceName"/>
<tiles:importAttribute name="items"/>

  <div class=side>
    <h3>news from <c:out value="${sourceName}"/></h3>

	<c:choose>
		<c:when test="${items == null}">
			<div class="news">Temporarily not available</div>
		</c:when>
		<c:otherwise>
			<c:forEach items="${items}" var="item">
				<div class="news"><a href="<c:out value="${item.link}"/>" target="_new"><c:out value="${item.title}"/></a></div>
			</c:forEach>
		</c:otherwise>
	</c:choose>
  </div>
