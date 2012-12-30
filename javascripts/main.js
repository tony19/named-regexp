// assumes Jquery is loaded at this point

$(function() {
	prettyPrint();

	// release notes
	$('.detail').hide();
	$('.summary :not(:first)').hide();

	$('#release-notes h3').click(function() {
		$(this).find('~ .summary').slideToggle();
	});
});
