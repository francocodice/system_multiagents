/* Initial beliefs and rules */

price(_Article,X) :- 
	.random(R) & X = (10*R)+100.
	
/* Initial goals */

!register.
!setup.

/* Initial goals */

+!setup <- 	.random(R);
	        +avaibleMoney(math.floor((R*100)+1500));
	        ?avaibleMoney(Tot);
			.print("Money avaible ", Tot).

+!register <- .df_register("participant");
              .df_subscribe("initiator").

@c1 +cfp(CNPId,Article,Price)[source(A)]
   :  provider(A,"initiator") & 
      price(Article,Offer) &
	  Offer > Price
   <- +proposal(CNPId,Article, math.floor(Offer)); 
      .send(A,tell,propose(CNPId,Article,math.floor(Offer))).

@r1 +accept_proposal(CNPId)
   :  proposal(CNPId,Article,Offer)
   <- .print("My proposal '",Offer,"' won CNP ",CNPId, " for ", Article,"!");
   	  +bought(Article,Offer).

@r2 +reject_proposal(CNPId)
   <- .print("I lost CNP ",CNPId, ".");
      -proposal(CNPId,_,_). 
	
+bought(Article, Price): avaibleMoney(X) <-
	-avaibleMoney(_);
	+avaibleMoney(X-Price);
	+article(Article);
	?avaibleMoney(TotSpent);
	.print("I bought ", Article, " for ", Price, "$. My avaible money are ", TotSpent).
