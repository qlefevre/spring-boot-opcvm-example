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
// Common to all tables
$(document).ready(function(){
	    $('.simpletable tr').click(function(){
	        window.location = $(this).find('td a').first().attr('href');
	        return false;
	    });
	    $('.complextable tr td').click(function(){
	    	var href = $(this).find('a').first().attr('href');
	    	if(href !== undefined){
	        	window.location = href;
	    	}
	        return false;
	    });
	});

var updateGraphTrigger = undefined;

var createTouchSpin = function(element){
	element.TouchSpin(
			{
                min: -999999,
                max: 999999,
                step: 0.01,
                decimals: 2
            });
	
	element.on("paste keyup", function(e) {
		   e.preventDefault();
		   var value = $(this).val();
		   if(value.indexOf(',') >= 0){
		   		$(this).val(value.replace(',','.'));
		   }
	});
	
	
	element.on('change',function(e){
		if(updateGraphTrigger === undefined){
			setTimeout(updateGraph,200);
			updateGraphTrigger = 'update';
		}
	});
}


// Alert page
var maxValue = function(max,values){
	for (i = 0; i < values.length; i++) {
		if(max < values[i]){
			max = values[i];
		}
	}
	return max;
} 
var minValue = function(min,values){
	for (i = 0; i < values.length; i++) {
		if(min > values[i]){
			min = values[i];
		}
	}
	return min;
} 


var updateGraph = function(){
	  updateGraphTrigger = undefined;
	  $("#opcvmchart").html('');
	  
	  data.goals =[];
	  data.goalLineColors = [];
	  var value = $('#type').val();
	  if('TRIGGER_ALERT' == value){
		  data.goals = [$("#value").val()];
		  var trend = $('#trend').val();
		  if('UP' == trend){
			  data.goalLineColors = ['#008000'];
	  	  }else{
	  		  data.goalLineColors = ['#ff0000'];
	  	  }
	  }else if('TUNNEL_ALERT' == value){
		  data.goals = [$("#minvalue").val(),$("#maxvalue").val()];
		  data.goalLineColors = ['#104e8b','#104e8b'];
	  }
	  
	  var ymin = minValue(data.origymin,data.goals);
	  var ymax = maxValue(data.origymax,data.goals);
	  ymin = ((ymin/5)|0)*5;
  	  ymax = (((ymax/5)|0)+1)*5;
  	  data.ymin = ymin;
  	  data.ymax = ymax;
	  
	  new Morris.Area(data); 
}

var hideElements = function(value){
	  $("#form-group-value").hide();
	  $("#form-group-change").hide();
	  $("#form-group-minvalue").hide();
	  $("#form-group-maxvalue").hide();
	  $("#form-group-trend").hide();

	  if('TRIGGER_ALERT' == value){
		  $("#form-group-value").show();
		  $("#form-group-trend").show();
	  }else if('CHANGE_ALERT' == value){
		  $("#form-group-change").show();
		  $("#form-group-trend").show();
	  }else if('TUNNEL_ALERT' == value){
		  $("#form-group-minvalue").show();
		  $("#form-group-maxvalue").show();
	  }
	  
	  updateGraph();
}



	