#include <stdio.h> 
#include <windows.h>

/*
 * Very naive program to read the content of a file
 * Used to mock padre-sw on Windows since there's no
 * equivalent of 'cat' except for 'type', but that's
 * a shell command and we can't run 'cmd.exe' easily
 *
 * @param First arg: Path to the file
 * @param Second arg (optional): Delay to wait in seconds.
 */
int main(int argc, char *argv[]) {
    FILE *f;
    int n;

	if (argc > 1) {
		Sleep(atoi(argv[2]));
	}
    
    f = fopen (argv[1], "r"); 

    if (f == NULL) {
        return 0;
    }

    while( (n = fgetc( f ) )  != EOF) {
        printf ("%c", n);
    }

    fclose(f);
    
    return 0;
}
