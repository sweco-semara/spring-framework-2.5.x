/*
 * Copyright 2002-2007 the original author or authors.
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
 */

package org.springframework.jdbc.core.simple.metadata;

import org.springframework.jdbc.core.simple.metadata.AbstractCallMetaDataProvider;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * @author trisberg
 */
public class SqlServerCallMetaDataProvider extends AbstractCallMetaDataProvider {

	private static final String REMOVABLE_COLUMN_PREFIX = "@";

	public SqlServerCallMetaDataProvider(DatabaseMetaData databaseMetaData) throws SQLException {
		super(databaseMetaData);
	}


	@Override
	protected String parameterNameToUse(String parameterName) {
		if (parameterName == null)
			return null;
		if (parameterName.length() > 1 && parameterName.startsWith(REMOVABLE_COLUMN_PREFIX))
			return super.parameterNameToUse(parameterName.substring(1));
		else
			return super.parameterNameToUse(parameterName);
	}
}