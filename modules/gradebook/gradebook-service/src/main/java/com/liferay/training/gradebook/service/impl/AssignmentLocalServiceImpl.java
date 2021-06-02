/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.training.gradebook.service.impl;

import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.dao.orm.Disjunction;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.training.gradebook.model.Assignment;
import com.liferay.training.gradebook.service.base.AssignmentLocalServiceBaseImpl;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * The implementation of the assignment local service.
 *
 * <p>
 * All custom service methods should be put in this class. Whenever methods are added, rerun ServiceBuilder to copy their definitions into the <code>com.liferay.training.gradebook.service.AssignmentLocalService</code> interface.
 *
 * <p>
 * This is a local service. Methods of this service will not have security checks based on the propagated JAAS credentials because this service can only be accessed from within the same VM.
 * </p>
 *
 * @author Pedro Blandim
 * @see AssignmentLocalServiceBaseImpl
 */
@Component(
	property = "model.class.name=com.liferay.training.gradebook.model.Assignment",
	service = AopService.class
)
public class AssignmentLocalServiceImpl extends AssignmentLocalServiceBaseImpl {
	
	public Assignment addAssignment(
			long groupId, Map<Locale, String> titleMap, Map<Locale, String> descriptionMap,
			Date dueDate, ServiceContext serviceContext) 
			throws PortalException {
		
		// Get group and user.
		
		Group group = groupLocalService.getGroup(groupId);
		long userId = serviceContext.getUserId();
		User user = userLocalService.getUser(userId);
		
		// Generate primary key for the assignment.
		long assignmentId = 
				counterLocalService.increment(Assignment.class.getName());
		
		// Create assignment. This doesn't yet persist the entity.
		Assignment assignment = createAssignment(assignmentId);
		
		// Populate fields.
		assignment.setCompanyId(group.getCompanyId());
		assignment.setCreateDate(serviceContext.getCreateDate(new Date()));
		assignment.setDueDate(dueDate);
		assignment.setDescriptionMap(descriptionMap);
		assignment.setGroupId(groupId);
		assignment.setModifiedDate(serviceContext.getModifiedDate(new Date()));
		assignment.setTitleMap(titleMap);
		assignment.setUserId(userId);
		assignment.setUserName(user.getScreenName());
		
		// Persist assignment to database.
		return super.addAssignment(assignment);
	}
	
	public Assignment updateAssignment(
			long assignmentId, Map<Locale, String> titleMap, Map<Locale, String> descriptionMap,
			Date dueDate, ServiceContext serviceContext) 
			throws PortalException {
		
		// Get the Assignemnt by id.
		Assignment assignment = getAssignment(assignmentId);
		
		// Set updated fields and modification date.
		assignment.setModifiedDate(new Date());
		assignment.setTitleMap(titleMap);
		assignment.setDueDate(dueDate);
		assignment.setDescriptionMap(descriptionMap);
		
		assignment = super.updateAssignment(assignment);
		
		return assignment;
	}
	
	public List<Assignment> getAssignmentByGroupId(long groupId) {
		return assignmentPersistence.findByGroupId(groupId);
	}
	
	public List<Assignment> getAssignmentByGroupId(
			long groupId, int start, int end) {
		return assignmentPersistence.findByGroupId(groupId, start, end);
	}
	
	public List<Assignment> getAssignmentByGroupId(
			long groupId, int start, int end,
			OrderByComparator<Assignment> orderByComparator) {
		return assignmentPersistence.findByGroupId(
				groupId, start, end, orderByComparator);
	}
	
	public List<Assignment> getAssignmentByKeywords(
			long groupId, String keywords, int start, int end,
			OrderByComparator<Assignment> orderByComparator) {
		
		return assignmentLocalService.dynamicQuery(
				getKeywordsSearchDynamicQuery(groupId, keywords), start, end,
				orderByComparator);
	}
	
	public long getAssignmentCountByKeywords(long groupId, String keywords) {
		return assignmentLocalService.dynamicQueryCount(
				getKeywordsSearchDynamicQuery(groupId, keywords));
	}
	
	private DynamicQuery getKeywordsSearchDynamicQuery(
			long groupId, String keywords) {
		
		DynamicQuery dynamicQuery = dynamicQuery().add(
				RestrictionsFactoryUtil.eq("groupId", groupId));
		
		if (Validator.isNotNull(keywords)) {
			Disjunction disjunctionQuery = 
					RestrictionsFactoryUtil.disjunction();
			
			disjunctionQuery.add(
					RestrictionsFactoryUtil.like("title","%"+keywords+"%"));
			disjunctionQuery.add(
					RestrictionsFactoryUtil.like("description","%"+keywords+"%"));
			
			dynamicQuery.add(disjunctionQuery);
		}
		
		return dynamicQuery;
	}
	
	// Silence generated methods
	@Override
	public Assignment addAssignment(Assignment assignment) {
		throw new UnsupportedOperationException("Not supported");
	}
	
	@Override
	public Assignment updateAssignment(Assignment assignment) {
		throw new UnsupportedOperationException("Not supported");
	}
	
}