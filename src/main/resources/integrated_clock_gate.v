module integrated_clock_gate( clkinp, d, clkout);

input clkinp;
input d;
output clkout;

reg latched_d;
   
always @(clkinp or d) begin
  if ( !clkinp) begin
     latched_d = d;
  end
end

assign clkout = clkinp & latched_d;

endmodule
