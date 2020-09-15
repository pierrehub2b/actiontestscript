var coll = document.getElementsByClassName("collapsible");
var i;

for (i = 0; i < coll.length; i++) {
  coll[i].innerText = "+ " + coll[i].innerText.split('.')[coll[i].innerText.split('.').length-1];
  coll[i].addEventListener("click", function() {
    this.classList.toggle("active");
    var content = this.nextElementSibling;
    if (content.style.maxHeight){
      content.style.maxHeight = null;
	  this.innerText = this.innerText.replace('-', '+')
    } else {
      content.style.maxHeight = content.scrollHeight + "px";
	  this.innerText = this.innerText.replace('+', '-')
    } 
  });
}