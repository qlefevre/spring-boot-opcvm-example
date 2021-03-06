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
package com.github.qlefevre.opcvm.admin;

import java.io.Serializable;

import com.github.qlefevre.crudadmin.CrudAdminDefaultObjectIdSerializer;

public class CustomObjectIdSerializer extends CrudAdminDefaultObjectIdSerializer {

	/** 
	 * Deserialize ID Object from the given String
	 * 
	 * @param value ID Object serialized
	 * @param type ID Object type
	 * @return ID Object from the given String
	 */
	@Override
	public Serializable fromString(String value, Class<?> type){
		Serializable result = value;
		if(type.equals(long.class) || type.equals(Long.class)){
			result = Long.valueOf(value,16);
		}else if(type.equals(int.class) || type.equals(Integer.class)){
			result = Integer.valueOf(value,16);
		}
		return result;
	}
	
	/** 
	 * Serialize ID Object as String
	 * 
	 * @param value ID Object
	 * @param type ID Object type
	 * @return a String that represents the serialized ID Object
	 */
	@Override
	public String toString(Object value, Class<?> type){
		if(type.equals(long.class) || type.equals(Long.class)){
			return Long.toHexString((long)value);
		}else if(type.equals(int.class) || type.equals(Integer.class)){
			return Integer.toHexString((int)value);
		}
		return value.toString();
	}
}
