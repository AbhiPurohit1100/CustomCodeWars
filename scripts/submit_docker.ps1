$code = @'
import java.util.*;
public class Main {
  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    long a = sc.nextLong(), b = sc.nextLong();
    System.out.print(a+b);
  }
}
'@
$body = @{ contestId=1; problemId=1; username='dockertest'; sourceCode=$code } | ConvertTo-Json -Depth 5
Invoke-RestMethod -UseBasicParsing -Method Post -ContentType 'application/json' -Uri 'http://localhost:8080/api/submissions' -Body $body | ConvertTo-Json -Depth 5
