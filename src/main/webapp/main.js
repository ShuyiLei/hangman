$( document ).ready(function() {
	$.get("WordServlet", {requestType:"gamestart"}, function(responseText) {   // Execute Ajax GET request on URL of WordServlet
	  // Initialize game, get the length of the string.
	  var lenth = parseInt(responseText);
	  // Network has its delay. Don't want to accept key input all the time
	  // When ready to accept input, busy is false
	  var busy = false;
	  // Append "_" according to letter num
	  for(i = 0; i < lenth; i ++) {
		  $("#word").append("<div class=\"letter\">_</div>");
	  }
	  // history_index is the index of the guess history
	  var history_index = 0;
	  $( document ).keydown(function(event){
		  if(event.which == 32){
			  // Space pressed. Reload
			  window.location.reload(true);
			  return;
		  }
		  // When busy, don't accept keyboard
		  if(!busy){
			  if($("#info").css("visibility") == 'hidden'){
				  $("#info").css('visibility', 'visible');
			  }

			  // guess is user's input, a string
			  var guess = String.fromCharCode(event.which).toUpperCase();
			  if( guess == guess.toLowerCase()){
				  // If it is not a letter key, tell user
				  // Non-letter key's lower case is same as its upper case
				  $("#info1").text("Please make sure to press a letter key");
				  $("#info2").text("");
			  }
			  else{
				  // A letter key is pressed.
				  $("#info1").text("You guessed " + guess + ": ");
				  $("#info2").text("Loading...");
				  // Block following key presses
				  busy = true;
				  $.get("WordServlet", {requestType:"guess",letter:guess}, function(responseJson) {
					  if(responseJson.res == "wrong" || responseJson.res == "failed"){
						// Record wrong guess histories
						var tab;
						if(history_index < 5){
							tab = $("#his_tab1");
						} else {
							tab = $("#his_tab2");
						}
						history_index++;
						tab.append("<tr><td>"+ history_index.toString() +"</td><td>" + guess + "</td></tr>");
					  }
					  busy = false;
					  if(responseJson.res == "wrong"){
						  $("#info2").text("Oops! Please try again:)");
						  // Change the image to corresponding one
						  $("#mainimg").attr("src","resources/"+ responseJson.state +".png");
						  
					  } else if (responseJson.res == "failed"){
						  $("#info1").text("The answer is " + responseJson.ans+". ");
						  $("#info2").text("Good luck next time!");
						  // Change the image to the final one
						  $("#mainimg").attr("src","resources/10.png");
						  busy = true;
					  } else if (responseJson.res == "ok" || responseJson.res == "pass"){
						  $("#info2").text("Bingo!");
						  // get the array of indexes of the letter
						  var lst = responseJson.detail;
						  for(i = 0; i < lst.length; i ++){
							  // replace _ to the letter
							  $(".letter").eq(lst[i]).text(guess);
						  }
						  if(responseJson.res == "pass"){
							  $("#info2").text("You win!");
							  busy = true;
						  }
					  } else if (responseJson.res == "guessed"){
						  $("#info2").text("You guessed this one!");
					  }
				  }).fail(function(){
					  // When session id is not found in server and the server prompt an error,
					  // reload.
					  window.location.reload(true);
				  });
			  }
		  }
		  
	  });
  }); 
});