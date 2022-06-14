/*
* Variabili globali
*/

	Calendar_list          = new Array();
	Calendar_mouseoverStatus = false;
	Calendar_mouseX          = 0;
	Calendar_mouseY          = 0;


/*
* Calendar(): costruttore
*
* @ Parametri:
* @ objName : String      		Nome dell'oggetto
* @ callbackFunc : String     	Nome della funzione esterna che ha il compito di processare la data selezionata
*/

function Calendar(objName, callbackFunc)
{
	/*
 	* Attributi
	*/
	// Data corrente
	this.today          = new Date();
	this.date           = this.today.getDate();
	this.month          = this.today.getMonth();
	this.year           = this.today.getFullYear();

	this.objName        = objName;
	this.callbackFunc   = callbackFunc;
	this.imagesPath     = 'css/calendar/img/';
	this.objID        = 'CalendarObj_' + Calendar_list.length;

	
	this.offsetX        = 10;
	this.offsetY        = 10;

	//Mese e Anno selezionati 
	this.currentMonth   = this.month;
	this.currentYear    = this.year;

	/*
       * Metodi pubblici
       */
	
      this.show              = Calendar_show;
	this.writeHTML         = Calendar_writeHTML;

	this.setOffset         = Calendar_setOffset;
	this.setOffsetX        = Calendar_setOffsetX;
	this.setOffsetY        = Calendar_setOffsetY;
	this.setCurrentMonth   = Calendar_setCurrentMonth;
	this.setCurrentYear    = Calendar_setCurrentYear;
	
	/*
       * Metodi privati
       */
	 
	this._getCalendar         	= Calendar_getCalendar;
	this._hideCalendar        	= Calendar_hideCalendar;
	this._showCalendar        	= Calendar_showCalendar;
	this._setCalendarPosition 	= Calendar_setCalendarPosition;
	this._setHTML			= Calendar_setHTML;

	this._getDaysInMonth   = Calendar_getDaysInMonth;
	this._mouseover        = Calendar_mouseover;

	Calendar_list[Calendar_list.length] = this;
	this.writeHTML();
}

/*
* Calendar_show()
* 	Visualizza (o aggiorna, se gi� visibile) il calendario. 
* 
* @ Parametri:
* @ month : Integer [Optional] [Range: 0..11]
* @ year  : Integer [Optional] [Formato: YYYY]
*/

function Calendar_show()
{
	var previousMonth, month, nextMonth;
	var previousYear, year, nextYear;
	var monthnames, numdays, currentDate, firstOfMonth;
	var dayTable, row, i, cssClass, dayCell ;
	var prevImg, nextImg, prevMonthLink, nextMonthLink, prevYearLink, nextYearLink;
	var monthCell, yearCell, html;
		
	this.currentMonth = month = arguments[0] != null ? arguments[0] : this.currentMonth;
	this.currentYear  = year  = arguments[1] != null ? arguments[1] : this.currentYear;

	monthnames = new Array('Gennaio', 'Febbraio', 'Marzo', 'Aprile', 'Maggio', 'Giugno', 'Luglio', 'Agosto', 'Settembre', 'Ottobre', 'Novembre', 'Dicembre');
	numdays    = this._getDaysInMonth(month, year);

	currentDate = new Date(year, month, 1);
	firstOfMonth = currentDate.getDay();

	/* Impostazione della variabile dayTable.
	 *	Tale variabile contiene il codice html necessario per la 
	 *	costruzione e visualizzazione della matrice dei giorni.
	 */

	dayTable = new Array(new Array());
	for(i=0; i< firstOfMonth; i++)
	{
		dayTable[0][dayTable[0].length] = '<td>&nbsp;</td>';
	}

	
	row = 0;
	i   = 1;
	while(i <= numdays)
	{
		if(dayTable[row].length == 7)
		{
			dayTable[++row] = new Array();
		}

		/*
            * Genero la cella html della tabella dei giorni
            */
		cssClass = (i == this.date && month == this.month && year == this.year) ? 'Calendar_today' : 'Calendar_day';
            dayCell = '<a href="javascript: ' + this.callbackFunc + '(' + i + ', ' + (Number(month) + 1) + ', ' + year + '); ' + this.objName + '._hideCalendar()">' + (i++) + '</a>';
			dayTable[row][dayTable[row].length] = '<td class="' + cssClass + '" align="center">' + dayCell + '</td>';
	}

	for(i=0; i<dayTable.length; i++)
	{
			dayTable[i] = dayTable[i].join('\n') + '\n';
	}
		
	//Fine dell'impostazione della variabile dayTable

	previousYear  = currentDate.getFullYear()- 1;
      nextYear = currentDate.getFullYear()+ 1 ;

	previousMonth = currentDate.getMonth() - 1;
	if(previousMonth < 0)
		previousMonth = 11;
	nextMonth = currentDate.getMonth() + 1;
	if(nextMonth > 11)
		nextMonth = 0;
		
	prevImg  = '<img src="' + this.imagesPath + '/prev.gif" border="0" />';
	nextImg  = '<img src="' + this.imagesPath + '/next.gif" border="0" />';
	prevMonthLink = '<a href="javascript: ' + this.objName + '.show(' + previousMonth + ', ' + currentDate.getFullYear() + ')">' + prevImg + '</a>';
	nextMonthLink = '<a href="javascript: ' + this.objName + '.show(' + nextMonth + ', ' + currentDate.getFullYear() + ')">' + nextImg + '</a>';
	prevYearLink = '<a href="javascript: ' + this.objName + '.show(' + currentDate.getMonth() + ', ' + previousYear + ')">' + prevImg + '</a>';
	nextYearLink = '<a href="javascript: ' + this.objName + '.show(' + currentDate.getMonth() + ', ' + nextYear + ')">' + nextImg + '</a>';
      monthCell = '<input class="Calendar_header" name="month" type="text" size="9" maxsize="9" value="'+monthnames[currentDate.getMonth()]+'" />';
	yearCell = '<input class="Calendar_header" name="year" type="text" size="4" maxsize="4" value="'+currentDate.getFullYear()+'" />';

	/*Impostazione della variabile html
	 *	Tale variabile contiene tutto il codice html necessario per la costruzione del calendario
       */

	html = '<table border="0" bgcolor="#eeeeee">';
	html += '<tr><td colspan="7" align="center">';
	html += prevMonthLink;
      html += monthCell;
      html += nextMonthLink; 
      html += '&nbsp;&nbsp;&nbsp;';
	html += prevYearLink;
	html += yearCell;
	html += nextYearLink;
	html += '</td><tr>';
	html += '<td class="Calendar_dayname">Dom</td>';
	html += '<td class="Calendar_dayname">Lun</td>';
	html += '<td class="Calendar_dayname">Mar</td>';
	html += '<td class="Calendar_dayname">Mer</td>';
	html += '<td class="Calendar_dayname">Gio</td>';
	html += '<td class="Calendar_dayname">Ven</td>';
	html += '<td class="Calendar_dayname">Sab</td></tr>';
	html += '<tr>' + dayTable.join('</tr>\n<tr>') + '</tr>';
	html += '</table>';

	this._setHTML(html);
	if (!arguments[0] && !arguments[1]) 
	{
		this._showCalendar();
		this._setCalendarPosition();
	}
}

/*
* Calendar_writeHTML()
* 	(1)Definisce il punto di attivazione del calendario.
*	(2)Definisce il box html, all'interno del documento, che contiene il calendario.
*/
	function Calendar_writeHTML()
	{
		if (is_ie5up || is_nav6up || is_gecko) {
			document.write('<a href="javascript: ' + this.objName + '.show()"><img src="' + this.imagesPath + 'Calendar.gif" border="0" width="16" height="16" /></a>');
			document.write('<div class="Calendar" id="' + this.objID + '" onmouseover="' + this.objName + '._mouseover(true)" onmouseout="' + this.objName + '._mouseover(false)"></div>');
		}
	}

/*
 * Calendar_setOffset...()
 * 	Definizione dell'offset del punto in cui il calendario viene visualizzato sullo schermo rispetto
 *    al "punto di click" di attivazione del calendario stesso. 
 */

/*
* Calendar_setOffset()
*
* @ Parametri:
* @ Xoffset : Integer	 [pixels]
* @ Yoffset : Integer    [pixels]
*/

function Calendar_setOffset(Xoffset, Yoffset)
{
	this.setOffsetX(Xoffset); //offset orizzonatle dalla posizione del mouse
	this.setOffsetY(Yoffset); //offset verticale dalla posizione del mouse

}

/*
* Calendar_setOffsetX
*
* @ Parametri:
* @ Xoffset : Integer [pixels] 
*/

function Calendar_setOffsetX(Xoffset)
{
	this.offsetX = Xoffset; //offset orizzonatle dalla posizione del mouse

}

/*
* Calendar_setOffsetY()
*
* @ Parametri:
* @ Yoffset : Integer [pixels]
*/

function Calendar_setOffsetY(Yoffset)
{
	this.offsetY = Yoffset; //offset verticale dalla posizione del mouse

}
	
/**
* Calendar_setCurrentMonth()
* 	Imposta il mese che deve essere visualizzato
*
* @ Parametri:
* @ month : Integer
*/
function Calendar_setCurrentMonth(month)
{
	this.currentMonth = month;
}

/*
* Calendar_setCurrentYear(year)
*	Imposta l'anno che deve essere visualizzato
*
* @ Parametri:
* @ year : Integer
*/

function Calendar_setCurrentYear(year)
{
	this.currentYear = year;
}

/*
* Calendar_getCalendar()
* 	Restituisce il box html, all'interno del documento, in cui � contenuto il calendario
*/
	
function Calendar_getCalendar()
{
	var objID = this.objID;
	if (document.getElementById(objID)) 
	{
		return document.getElementById(objID);
	} 
	else if (document.all(objID)) 
	{
		return document.all(objID);
	}
}

/**
* Calendar_hideCalendar()
* 	Nasconde il box html in cui � contenuto il calendario
*
*/
function Calendar_hideCalendar()
{
	this._getCalendar().style.visibility = 'hidden';
}

/**
* Calendar_showCalendar()
*	 Mostra il box html in cui � contenuto il calendario
*
*/
function Calendar_showCalendar()
{
	this._getCalendar().style.visibility = 'visible';
}

/**
* Calendar_setCalendarPosition()
*	Imposta la posizione sullo schermo del calendario
*/
function Calendar_setCalendarPosition()
{
	this._getCalendar().style.top  = (Calendar_mouseY + this.offsetY) + 'px';
	this._getCalendar().style.left = (Calendar_mouseX + this.offsetX) + 'px';
}

/**
* Calendar_setHTML()
* Imposta la propriet� "innerHTML" per il box html in cui � contenuto il calendario
*
*/

function Calendar_setHTML(html)
{
	this._getCalendar().innerHTML = html;
}

/**
* Calendar_getDaysInMonth()
* 	Restituisce il numero di giorni presenti nel mese selezionato 
*
* @ Parametri:
* @ month : Integer  
* @ year  : Integer
*/
function Calendar_getDaysInMonth(month, year)
{
	monthdays = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
	if (month != 1) 
	{
		return monthdays[month];
	} 
	else 
	{
		return ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0 ? 29 : 28);
	}
}

/**
* onMouse(Over|Out): event handler
*
* @ Parametri:	
* @ status: Boolean  // True se il mouse � sopra il calendario; false altrimenti.
*/
function Calendar_mouseover(status)
{
	Calendar_mouseoverStatus = status;
	return true;
}

/**
* onMouseMove:  event handler
*/
	Calendar_oldOnmousemove = document.onmousemove ? document.onmousemove : new Function;

	document.onmousemove = function ()
	{
		if (is_ie5up || is_nav6up || is_gecko) {
			if (arguments[0]) {
				Calendar_mouseX = arguments[0].pageX;
				Calendar_mouseY = arguments[0].pageY;
			} else {
				Calendar_mouseX = event.clientX + document.body.scrollLeft;
				Calendar_mouseY = event.clientY + document.body.scrollTop;
				arguments[0] = null;
			}
	
			Calendar_oldOnmousemove();
		}
	}

/**
* document.onclick : eventi handler
*/
	Calendar_oldOnclick = document.onclick ? document.onclick : new Function;

	document.onclick = function ()
	{
		if (is_ie5up || is_nav6up || is_gecko) 
		{
			if(!Calendar_mouseoverStatus)
			{
				for(i=0; i<Calendar_list.length; ++i)
					Calendar_list[i]._hideCalendar();
				
			}
	
		Calendar_oldOnclick(arguments[0] ? arguments[0] : null);
		}
	}
