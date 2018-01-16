result={ready:false};
if(typeof window != 'undefined' && window.document){
	result.ready=window.document.readyState=='complete';
}