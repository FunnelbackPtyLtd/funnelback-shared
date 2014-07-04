//NOTE: parameters set on page: contentOptimiser

$(document).ready(function() {
	
			var timer;
			
			$(document) //START document daisy chain
			
			.on('click','.link-collections', function(){
				var catchTarget = $('#catch-collection-list');
				
				// prevent loading when not needed to
				if (!catchTarget.hasClass('loaded')){
	
					$.get('?',function(data){
						var list = $(data).find('.fb-list').html();
						
						list = list.replace(/collection=/g,'query=' + contentOptimiser.target_url + '&collection=');
						
						catchTarget.html(list).addClass('loaded');
					});
	
				} //end check for loaded 
 				//return false;
				
			})
			
			.on('show.bs.collapse','.panel-collapse', function () {
				$('.panel-collapse.in').attr('data-mathy','steve'+Math.ceil(Math.random()) + 2);
				//alert();
				// do something
			})

			//Accordion on Optimisation Advice Section - Make clickable and assign designated CSS classes 
			.on('click','#accordion .panel.inactive',function(e){
				
				id = $(e.target).attr('href');        
				chartdiv_id = $(id).find('.chartdiv').attr('id');                        
				doChart(chartdiv_id, true);
		
					if($('.panel-collapse.in').length)
					$('.panel-collapse.in').collapse('hide');
					
					
					if($(this).hasClass('inactive'))
					$(this).find('.panel-collapse').collapse('show');
			
			})
			//Accordion on Optimisation Advice Section - on collapse do this
			.on('show.bs.collapse','.panel-collapse', function(){
					$(this).parents('.panel.inactive').removeClass('inactive').addClass('active');
			})
			
			//Accordion on Optimisation Advice Section - on hide do this
			.on('hide.bs.collapse','.panel-collapse', function(){
					$(this).parents('.panel').addClass('inactive').removeClass('active');
			})
			
			// Click on Query and URL Fields in the Content Summary 
			.on('click','#co-cs-form',function(){
	
				var a = $(this); 
				var b = 'disguise';
				
				//add classes to show the form via CSS
				a.addClass('active');	
				if(a.hasClass(b)){
					a.removeClass(b);
					$('#co-cs-summary').hide();
					
					// maybe needed if not working as expected 
					//setTimeout(function(){$('#form_query').focus();},200);
					
				// focus in on the form on conditions met
				var targetFormQuery = $(this).find('#form_query');
				var targetFormURL = $(this).find('#form_url');
				
				if (!$(targetFormQuery).is(':focus') && !$(targetFormURL).is(':focus')){
					$(targetFormQuery).focus();
					}
				}
				
	
			})
			
			// Unfocus on Query and URL Fields in the Content Summary
			.on('focusout','#co-cs-form',function(){
				var a = $(this); 
				var b = 'disguise';
				
				setTimeout(function(){
					if(a.hasClass('active')){}
					else
					{
					a.addClass(b);
					$('#co-cs-summary').show();	
					}
				},500);
				
				if(!a.hasClass(b)){
				a.removeClass('active');
				}
				
			})
			// Ajax the loading screen on submit of the form?
			// not sure yet need to configure a window.hash change event or maybe a HTML5 window pushState
			// should target $('#app') as its a global wrapper if so
			// for now we won't worry too much about it. ~steve
		
			//.on('submit','#co-cs-form',function(){
			//				var c = $(this).serializeArray();
			//				 
			//				var jqxhr = $.ajax( "example.php" )
			//					.done(function() {
			//					alert( "success" );
			//					})
			//					.fail(function() {
			//					alert( "error" );
			//					})
			//					.always(function() {
			//					alert( "complete" );
			//					});
			//					
			//					jqxhr.always(function() {
			//					alert( "second complete" );
			//					});
			//
			//				return false;
			//			})
			
			
			// Example prompt using bootboxjs, reduces the need for extra HTML 
			.on('click','.navbar-brand',function(){
				bootbox.prompt("What is your name?", function(result) {
					if (result === null) {
					console.log("Prompt dismissed");
					} else {
					console.log("Prompt enabled and done with result:" + result);
					
					}
				});	
			})
			
				
			; // End 'document' daisy chain 

			//Last but not least, close tip panels after 1.5 seconds, they should be filled with charts by now 

			setTimeout(function(){
			$('.panel-collapse').addClass('collapse');
			},1500);
			
}); // End 'document' ready