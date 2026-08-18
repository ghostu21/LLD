# Recommendation Service — Interview questions

Senior-style follow-ups. Point at this codebase when you answer.

---

### Ranking

1. Why Strategy instead of `if (placement == HOME)` in the controller?  
   *Open/closed: new rankers register in the factory.*
2. Item–item vs user–user CF?  
   *Item–item is stabler with more users than items; easier not to emit neighbor identities.*
3. How do you stop popularity snowballs?  
   *Diversity cap, experiment, down-weight already-viral SKUs, exploration.*

### Product / ML ops

4. What is candidate generation vs ranking?  
   *At scale you do not score the whole catalog online; this LLD does because N is tiny.*
5. How would you A/B this?  
   *Sticky `ExperimentAssigner`; primary metric CVR, guardrail diversity/complaint rate.*

### Security / privacy

6. Why is `userId` in the JSON body not enough?  
   *IDOR — see `AccessScenario`.*
7. Is a recommendation slate personal data?  
   *It is inferred profiling. Don’t log emails next to slates; honor HIDE as erasure of that signal.*
8. How do you stop scrapers reconstructing a catalog via recs?  
   *Rate limit, session, eligibility (no banned), don’t return internal scores to untrusted clients in production (we return scores here for teaching).*

### Reliability

9. Ranker timeout?  
   *Fallback decorator / popularity; circuit breaker in front of a remote model.*
10. Cache stampede?  
    *TTL + single-flight per key (not implemented; mention it).*

### Concurrency

11. Are `ConcurrentHashMap` stores enough?  
    *For the demo yes. Production: interaction log is Kafka; profiles are a feature store; ranking is stateless.*
