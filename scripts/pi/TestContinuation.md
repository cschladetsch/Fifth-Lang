# Test Continuations
A [Continuation](https://en.wikipedia.org/wiki/Continuation) represents 'the rest of the program'.

## Make a 'function' that just returns 1
```pi
{1} 'a= a& 1 == assert
```

## Example
```pi
{+} 'b=
a& 2 b& 3 == assert

# Make a continuation that doubles what's on the stack
{ 2 * } 'd =
2 d& 4 == assert
2 d& d& 8 == assert

# Can also use locals in continuations.
# This stores whatever is on the stack to a local called 'a',
# then uses it to calculate 'a*2 + a'
{ 'a= a 2 * a + } 'e =
3 e& 9 == assert

depth 0 == assert
"Done" print
```

## Test Resume
TODO: currently fails
```
{ 1 ... 2 3 } 'a=
a&
1 depth == assert
"Done resume" print
```